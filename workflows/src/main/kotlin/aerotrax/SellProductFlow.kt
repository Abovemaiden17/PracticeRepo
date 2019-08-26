package aerotrax

import aerotrax.contracts.ProductContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.ProductState
import aerotrax.states.UserState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.serialize
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class SellProductFlow (private val productId: String,
                       private val sellProductId: String,
                       private val sellerId: String,
                       private val buyerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val partyB = stringToParty("PartyB")
        val session = initiateFlow(partyB)
        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
        subFlow(FinalityFlow(stx, listOf(session)))
        subFlow(UpdateSellerWalletFlow(sellProductId, sellerId))
        subFlow(UpdateBuyerWalletFlow(sellProductId, buyerId))
        return subFlow(RemoveProductFlow(productId))
    }

    private fun outState(): ProductState
    {
        val partyB = stringToParty("PartyB")
        val product = inputProductRefUsingLinearID(stringToLinearID(productId)).state.data
        return product.copy(ownerId = stringToLinearID(buyerId), participants = listOf(partyB))
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val partyB = stringToParty("PartyB")
        val cmd = Command(ProductContract.Commands.Sell(), listOf(ourIdentity.owningKey, partyB.owningKey))
        addOutputState(outState(), ProductContract.PRODUCT_ID)
        addCommand(cmd)
    }

}

@InitiatedBy(SellProductFlow::class)
class SellProductFlowResponder(private val flowSession: FlowSession): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        subFlow(object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction)
            {
            }
        })
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}