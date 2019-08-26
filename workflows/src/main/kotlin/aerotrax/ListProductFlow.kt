package aerotrax

import aerotrax.contracts.SellProductContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.SellProductState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

@InitiatingFlow
@StartableByRPC
class ListProductFlow (private val serialId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val partyB = stringToParty("PartyB")
        val session = initiateFlow(partyB)
        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
        return subFlow(FinalityFlow(stx, listOf(session)))
    }

    private fun outState(): SellProductState
    {
        val product = inputProductRefUsingSerialId(serialId)!!.state.data
        val partyB = stringToParty("PartyB")
        return SellProductState(
                amount = Amount.fromDecimal(10.toBigDecimal(), Currency.getInstance("USD")),
                serialId = product.serialId,
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity, partyB)
        )
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val partyB = stringToParty("PartyB")
        val cmd = Command(SellProductContract.Commands.List(), listOf(ourIdentity.owningKey, partyB.owningKey))
        addOutputState(outState(), SellProductContract.SELL_ID)
        addCommand(cmd)
    }
}

@InitiatedBy(ListProductFlow::class)
class ListProductFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
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