package aerotrax

import aerotrax.contracts.UserContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.UserState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

@InitiatingFlow
@StartableByRPC
class UpdateSellerWalletFlow (private val sellProdId: String,
                             private val sellerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val session = initiateFlow(stringToParty("PartyB"))
        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
        return subFlow(FinalityFlow(stx, listOf(session)))
    }

    private fun newStateSeller(): UserState
    {
        val seller = inputUserRefUsingLinearID(stringToLinearID(sellerId)).state.data
        val sellProduct = inputSellProductRefUsingLinearID(stringToLinearID(sellProdId)).state.data
        val newAmount = Amount.fromDecimal((seller.wallet.quantity / 100).toBigDecimal(), seller.wallet.token) + Amount.fromDecimal((sellProduct.amount.quantity / 100).toBigDecimal(), sellProduct.amount.token)
        println(newAmount)
        return seller.copy(wallet = newAmount)
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val partyB = stringToParty("PartyB")
        val seller = inputUserRefUsingLinearID(stringToLinearID(sellerId))
        val cmd = Command(UserContract.Commands.Update(), listOf(ourIdentity.owningKey, partyB.owningKey))
        addInputState(seller)
        addOutputState(newStateSeller(), UserContract.USER_ID)
        addCommand(cmd)
    }
}

@InitiatedBy(UpdateSellerWalletFlow::class)
class UpdateSellerWalletFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
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