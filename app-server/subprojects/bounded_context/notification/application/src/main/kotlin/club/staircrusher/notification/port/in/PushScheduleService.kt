package club.staircrusher.notification.port.`in`

import club.staircrusher.notification.domain.model.PushNotificationSchedule
import club.staircrusher.notification.port.out.persistence.PushNotificationScheduleRepository
import club.staircrusher.notification.port.`in`.result.FlattenedPushSchedule
import club.staircrusher.slack.application.port.out.web.SlackService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.Instant

@Component
class PushScheduleService(
    private val transactionManager: TransactionManager,
    private val pushNotificationScheduleRepository: PushNotificationScheduleRepository,
    private val slackService: SlackService,
    @Value("\${scc.slack.channel.alert:#scc-server-alert}") val alertChannel: String,
) {
    fun list(
        limit: Int?,
        cursorValue: String?,
    ) = transactionManager.doInTransaction(isReadOnly = true) {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        // chunking 을 고려하여 limit 를 두배로 설정
        val overFetchLimit = normalizedLimit * 2
        val pageRequest = PageRequest.of(
            0,
            overFetchLimit,
        )
        println(cursor.timestamp)
        val result = pushNotificationScheduleRepository.findCursored(
            cursorCreatedAt = cursor.timestamp,
            cursorId = cursor.id,
            pageable = pageRequest,
        )
        val grouped = result.content
            .groupBy { it.groupId }
            .map { (groupId, schedules) ->
                FlattenedPushSchedule(
                    groupId = groupId,
                    scheduledAt = schedules.first().scheduledAt,
                    sentAt = schedules.first().sentAt,
                    title = schedules.first().title,
                    body = schedules.first().body,
                    deepLink = schedules.first().deepLink,
                    userIds = schedules.flatMap { it.userIds },
                    createdAt = schedules.first().createdAt,
                )
            }
        val selected = grouped.take(normalizedLimit)

        val nextCursor = if (result.hasNext()) {
            Cursor(selected[normalizedLimit - 1])
        } else {
            null
        }

        return@doInTransaction selected to nextCursor?.value
    }

    fun get(groupId: String) = transactionManager.doInTransaction(isReadOnly = true) {
        val pushNotificationSchedules = pushNotificationScheduleRepository.findByGroupId(groupId)
        if (pushNotificationSchedules.isEmpty()) {
            throw SccDomainException("$groupId 에 해당하는 푸시 알림 스케줄이 존재하지 않습니다.")
        }

        FlattenedPushSchedule(
            groupId = pushNotificationSchedules.first().groupId,
            scheduledAt = pushNotificationSchedules.first().scheduledAt,
            sentAt = pushNotificationSchedules.first().sentAt,
            title = pushNotificationSchedules.first().title,
            body = pushNotificationSchedules.first().body,
            deepLink = pushNotificationSchedules.first().deepLink,
            userIds = pushNotificationSchedules.flatMap { it.userIds },
            createdAt = pushNotificationSchedules.first().createdAt,
        )
    }

    fun getOutstandingSchedules() = transactionManager.doInTransaction(isReadOnly = true) {
        // cronjob 에서 10분에 한번 실행되는데, cronjob 에서 만드는 것은 curl container 이고
        // 실제 요청이 server pod 에 도달하는 시점은 몇초 지난 시점이다. 따라서 now 를 기준으로 삼아도 괜찮다
        val now = SccClock.instant()
        val schedules = pushNotificationScheduleRepository.findAllByScheduledAtBeforeAndSentAtIsNull(now)

        schedules
            .filter { it.scheduledAt.isBefore(now - scheduledPushNotificationInterval) }
            .let { outdatedSchedules ->
                if (outdatedSchedules.isNotEmpty()) {
                    alertOutdatedPushSchedules(outdatedSchedules)
                }
            }

        schedules
    }

    fun create(
        scheduledAt: Instant,
        title: String?,
        body: String,
        deepLink: String?,
        userIds: List<String>,
    ) = transactionManager.doInTransaction {
        val now = SccClock.instant()
        if (scheduledAt.isBefore(now)) throw SccDomainException("스케줄링 시간은 현재 시간 이후여야 합니다.")
        checkDuplicateSchedule(now, scheduledAt, title, body, deepLink, userIds)

        val groupId = EntityIdGenerator.generateRandom()

        val chunkedUserIds = userIds.chunked(CHUNK_SIZE)
        val pushNotificationSchedules = chunkedUserIds.mapNotNull {
            if (it.isEmpty()) return@mapNotNull null

            PushNotificationSchedule(
                id = EntityIdGenerator.generateRandom(),
                groupId = groupId,
                scheduledAt = scheduledAt,
                title = title,
                body = body,
                deepLink = deepLink,
                userIds = it,
            )
        }
        pushNotificationScheduleRepository.saveAll(pushNotificationSchedules).toList()
    }

    fun update(
        groupId: String,
        scheduledAt: Instant,
        title: String?,
        body: String,
        deepLink: String?,
    ) = transactionManager.doInTransaction {
        val now = SccClock.instant()
        val pushNotificationSchedules = pushNotificationScheduleRepository.findByGroupId(groupId)
        if (pushNotificationSchedules.isEmpty()) {
            throw SccDomainException("$groupId 에 해당하는 푸시 알림 스케줄이 존재하지 않습니다.")
        }

        if (pushNotificationSchedules.any { it.isSent() }) throw SccDomainException("이미 전송된 푸시 알림 스케줄은 수정할 수 없습니다.")
        if (pushNotificationSchedules.any { it.scheduledAt.isBefore(now) }) throw SccDomainException("이미 전송된 푸시 알림 스케줄은 수정할 수 없습니다.")
        if (scheduledAt.isBefore(now)) throw SccDomainException("스케줄링 시간은 현재 시간 이후여야 합니다.")

        pushNotificationSchedules.forEach {
            it.apply {
                this.scheduledAt = scheduledAt
                this.title = title
                this.body = body
                this.deepLink = deepLink
            }
        }
        pushNotificationScheduleRepository.saveAll(pushNotificationSchedules)
    }

    fun updateSentAt(id: String, sentAt: Instant) = transactionManager.doInTransaction {
        val pushNotificationSchedule = pushNotificationScheduleRepository.findByIdOrNull(id)
            ?: throw SccDomainException("$id 에 해당하는 푸시 알림 스케줄이 존재하지 않습니다.")

        pushNotificationSchedule.updateSentAt(sentAt)
        pushNotificationScheduleRepository.save(pushNotificationSchedule)
    }

    fun delete(groupId: String) = transactionManager.doInTransaction {
        val now = SccClock.instant()
        val pushNotificationSchedules = pushNotificationScheduleRepository.findByGroupId(groupId)
        if (pushNotificationSchedules.isEmpty()) {
            throw SccDomainException("$groupId 에 해당하는 푸시 알림 스케줄이 존재하지 않습니다.")
        }

        if (pushNotificationSchedules.any { it.isSent() }) throw SccDomainException("이미 전송된 푸시 알림 스케줄은 삭제할 수 없습니다.")
        if (pushNotificationSchedules.any { it.scheduledAt.isBefore(now) }) throw SccDomainException("이미 전송된 푸시 알림 스케줄은 삭제할 수 없습니다.")

        pushNotificationScheduleRepository.deleteAllByGroupId(groupId)
    }

    private fun checkDuplicateSchedule(
        now: Instant,
        scheduledAt: Instant,
        title: String?,
        body: String,
        deepLink: String?,
        userIds: List<String>,
    ) {
        val recentlyCreatedSchedules = pushNotificationScheduleRepository.findAllByCreatedAtAfterOrderByCreatedAtDesc(
            now.minusSeconds(60L),
        )

        val existsDuplicate = recentlyCreatedSchedules.any {
            it.scheduledAt == scheduledAt &&
            it.title == title &&
            it.body == body &&
            it.deepLink == deepLink &&
            it.userIds == userIds
        }

        if (existsDuplicate) {
            throw SccDomainException("이미 동일한 시간에 동일한 사용자에게 동일한 내용의 푸시 알림이 스케줄링 되어 있습니다.")
        }
    }

    private fun alertOutdatedPushSchedules(outdatedSchedules: List<PushNotificationSchedule>) {
        val message = "[${SccEnv.getEnv().name}]\n${outdatedSchedules.size} push notification schedules are outdated longer than " +
            "$scheduledPushNotificationInterval (${outdatedSchedules.joinToString { it.id }})"

        slackService.send(alertChannel, message)
    }

    private data class Cursor(
        val createdAt: Instant,
        val groupId: String,
    ) : TimestampCursor(createdAt, groupId) {
        constructor(schedule: FlattenedPushSchedule) : this(
            createdAt = schedule.createdAt,
            groupId = schedule.groupId,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        // 스케쥴링 된 푸시 알림을 보내는 주기
        private val scheduledPushNotificationInterval = Duration.ofMinutes(10L)
        private const val DEFAULT_LIMIT = 50
        private const val CHUNK_SIZE = 1000
    }
}
