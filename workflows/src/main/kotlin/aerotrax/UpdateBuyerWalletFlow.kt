package aerotrax

import aerotrax.contracts.UserContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.UserState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

@StartableByRPC
class UpdateBuyerWalletFlow (private val sellProdId: String,
                             private val buyerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
//        val session = initiateFlow(stringToParty("PartyA"))
//        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
//        return subFlow(FinalityFlow(stx, listOf(session)))
        return subFlow(FinalityFlow(verifyAndSign(builder()), listOf()))
    }

    private fun newStateBuyer(): UserState
    {
        val buyer = inputUserRefUsingLinearID(stringToLinearID(buyerId)).state.data
        val sellProduct = inputSellProductRefUsingLinearID(stringToLinearID(sellProdId)).state.data
        val x = (buyer.wallet.quantity - sellProduct.amount.quantity).toString()
//        return buyer.copy(wallet = Amount(buyer.wallet.quantity - sellProduct.amount.quantity, Currency.getInstance("USD")))
        return buyer.copy(wallet = Amount.parseCurrency("$$x"))
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val buyer = inputUserRefUsingLinearID(stringToLinearID(buyerId))
        val cmd = Command(UserContract.Commands.Update(), listOf(ourIdentity.owningKey))
        addInputState(buyer)
        addOutputState(newStateBuyer(), UserContract.USER_ID)
        addCommand(cmd)
    }
}