package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.CreateChallengeRequest
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class CreateChallengeUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
) {
    fun handle(createRequest: CreateChallengeRequest): Challenge = transactionManager.doInTransaction {
        val challengeFromInvitationCode = createRequest.invitationCode?.let {
            challengeRepository.findByInvitationCode(it)
        }
        if (challengeFromInvitationCode != null) {
            throw SccDomainException(
                "해당 참여코드로 만든 챌린지가 이미 존재합니다.(${createRequest.invitationCode})",
                errorCode = SccDomainException.ErrorCode.INVALID_ARGUMENTS
            )
        }
        challengeRepository.save(Challenge.of(createRequest))
    }
}
