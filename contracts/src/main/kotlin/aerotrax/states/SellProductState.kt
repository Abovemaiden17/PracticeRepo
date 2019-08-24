package aerotrax.states

import aerotrax.contracts.SellProductContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.util.*

@BelongsToContract(SellProductContract::class)
data class SellProductState(val amount: Amount<Currency>,
                            val serialId: String,
                            override val linearId: UniqueIdentifier,
                            override val participants: List<Party>): LinearState