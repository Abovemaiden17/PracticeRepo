package sample.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import sample.contracts.SampleContract

@BelongsToContract(SampleContract::class)
data class SampleState(val message: String,
                       override val linearId: UniqueIdentifier,
                       override val participants: List<Party>): LinearState