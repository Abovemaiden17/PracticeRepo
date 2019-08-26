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
class UpdateBuyerWalletFlow (private val sellProdId: String,
                             private val buyerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val session = initiateFlow(stringToParty("PartyB"))
        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
        return subFlow(FinalityFlow(stx, listOf(session)))
    }

    private fun newStateBuyer(): UserState
    {
        val buyer = inputUserRefUsingLinearID(stringToLinearID(buyerId)).state.data
        val sellProduct = inputSellProductRefUsingLinearID(stringToLinearID(sellProdId)).state.data
        val newAmount = Amount.fromDecimal((buyer.wallet.quantity / 100).toBigDecimal(), buyer.wallet.token) - Amount.fromDecimal((sellProduct.amount.quantity / 100).toBigDecimal(), sellProduct.amount.token)
        println(newAmount)
        return buyer.copy(wallet = newAmount)
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val partyB = stringToParty("PartyB")
        val buyer = inputUserRefUsingLinearID(stringToLinearID(buyerId))
        val cmd = Command(UserContract.Commands.Update(), listOf(ourIdentity.owningKey, partyB.owningKey))
        addInputState(buyer)
        addOutputState(newStateBuyer(), UserContract.USER_ID)
        addCommand(cmd)
    }
}

@InitiatedBy(UpdateBuyerWalletFlow::class)
class UpdateBuyerWalletFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
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