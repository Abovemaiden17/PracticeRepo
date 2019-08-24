package aerotrax.states

import aerotrax.contracts.UserContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.util.*

@BelongsToContract(UserContract::class)
data class UserState(val name: String,
                     val companyName: String,
                     val wallet: Amount<Currency>,
                     override val linearId: UniqueIdentifier,
                     override val participants: List<Party>): LinearState