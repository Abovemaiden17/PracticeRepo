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
        session.send(UserWallet(sellProductId, buyerId))
        subFlow(FinalityFlow(stx, listOf(session)))
        subFlow(UpdateSellerWalletFlow(sellProductId, sellerId))
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
        val cmd = Command(ProductContract.Commands.Sell(), listOf(partyB.owningKey))
        addOutputState(outState(), ProductContract.PRODUCT_ID)
        addCommand(cmd)
    }

//    private fun newStateBuyer(): UserState
//    {
//        val buyer = inputUserRefUsingLinearID(stringToLinearID(buyerId)).state.data
//        val sellProduct = inputSellProductRefUsingLinearID(stringToLinearID(sellProductId)).state.data
//        val x = (buyer.wallet.quantity - sellProduct.amount.quantity).toString()
////        return buyer.copy(wallet = Amount(buyer.wallet.quantity - sellProduct.amount.quantity, Currency.getInstance("USD")))
//        return buyer.copy(wallet = Amount.parseCurrency("$$x"))
//    }
}

@InitiatedBy(SellProductFlow::class)
class SellProductFlowResponder(private val flowSession: FlowSession): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val user = flowSession.receive<UserWallet>().unwrap { it }
        subFlow(UpdateBuyerWalletFlow(user.sellProductId, user.buyerId))
        subFlow(object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction)
            {
            }
        })
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}

@CordaSerializable
data class UserWallet(val sellProductId: String, val buyerId: String)