package aerotrax.states

import aerotrax.contracts.ProductContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(ProductContract::class)
data class ProductState(val prodName: String,
                        val serialId: String,
                        val ownerId: UniqueIdentifier,
                        override val linearId: UniqueIdentifier,
                        override val participants: List<Party>): LinearState