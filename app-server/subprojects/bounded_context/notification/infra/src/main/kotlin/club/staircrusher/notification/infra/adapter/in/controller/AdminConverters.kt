@file:Suppress("TooManyFunctions")

package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AdminPushNotificationScheduleDTO
import club.staircrusher.notification.port.`in`.result.FlattenedPushSchedule

fun FlattenedPushSchedule.toAdminDTO() = AdminPushNotificationScheduleDTO(
    id = groupId,
    scheduledAt = scheduledAt.toDTO(),
    sentAt = sentAt?.toDTO(),
    title = title,
    body = body,
    deepLink = deepLink,
    targetUsersCount = userIds.size,
)
