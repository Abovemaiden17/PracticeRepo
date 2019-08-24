package aerotrax.functions

import aerotrax.states.ProductState
import aerotrax.states.SellProductState
import aerotrax.states.UserState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

abstract class FlowFunctions : FlowLogic<SignedTransaction>()
{
    override val progressTracker = ProgressTracker(
            CREATING, VERIFYING, SIGNING, NOTARIZING, FINALIZING
    )

    fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }

    fun stringToParty(name: String): Party {
        return serviceHub.identityService.partiesFromName(name, false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for $name")
    }

    fun stringToLinearID(id: String): UniqueIdentifier {
        return UniqueIdentifier.fromString(id)
    }

    fun inputUserRefUsingLinearID(id: UniqueIdentifier): StateAndRef<UserState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<UserState>(criteria = criteria).states.single()
    }

    fun inputProductRefUsingLinearID(id: UniqueIdentifier): StateAndRef<ProductState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<ProductState>(criteria = criteria).states.single()
    }

    fun inputSellProductRefUsingLinearID(id: UniqueIdentifier): StateAndRef<SellProductState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<SellProductState>(criteria = criteria).states.single()
    }

    fun inputProductRefUsingSerialId(serialId: String): StateAndRef<ProductState>? {
        val criteria = QueryCriteria.VaultQueryCriteria()
        return serviceHub.vaultService.queryBy<ProductState>(criteria = criteria).states.find {
            stateAndRef -> stateAndRef.state.data.serialId == serialId
        }
    }
}