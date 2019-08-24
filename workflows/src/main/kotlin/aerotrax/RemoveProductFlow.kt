package aerotrax

import aerotrax.contracts.ProductContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.ProductState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class RemoveProductFlow (private val productId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        return subFlow(FinalityFlow(verifyAndSign(builder()), listOf()))
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val product = inputProductRefUsingLinearID(stringToLinearID(productId))
        val cmd = Command(ProductContract.Commands.Remove(), listOf(ourIdentity.owningKey))
        addInputState(product)
        addCommand(cmd)
    }
}