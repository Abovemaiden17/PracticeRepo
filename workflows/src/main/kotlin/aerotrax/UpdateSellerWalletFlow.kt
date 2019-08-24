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
class UpdateSellerWalletFlow (private val sellProdId: String,
                             private val sellerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        return subFlow(FinalityFlow(verifyAndSign(builder()), listOf()))
    }

    private fun newStateSeller(): UserState
    {
        val seller = inputUserRefUsingLinearID(stringToLinearID(sellerId)).state.data
        val sellProduct = inputSellProductRefUsingLinearID(stringToLinearID(sellProdId)).state.data
        val x = (seller.wallet.quantity - sellProduct.amount.quantity)
//        return seller.copy(wallet = Amount(seller.wallet.quantity + sellProduct.amount.quantity, Currency.getInstance("USD")))
        return seller.copy(wallet = Amount.parseCurrency("$$x"))
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val seller = inputUserRefUsingLinearID(stringToLinearID(sellerId))
        val cmd = Command(UserContract.Commands.Update(), listOf(ourIdentity.owningKey))
        addInputState(seller)
        addOutputState(newStateSeller(), UserContract.USER_ID)
        addCommand(cmd)
    }
}