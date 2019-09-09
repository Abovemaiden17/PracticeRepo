package sample

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import sample.contracts.SampleContract
import sample.functions.SampleFunctions
import sample.states.SampleState

@InitiatingFlow
@StartableByRPC
class SendFlow (private val counterParty: String): SampleFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val ptx = verifyAndSign(builder())
        val session = listOf(initiateFlow(stringToParty(counterParty)))
        val stx = subFlow(CollectSignaturesFlow(ptx, session))
        return subFlow(FinalityFlow(stx, session))
    }

    private fun outState(): SampleState
    {
        return SampleState(
                message = "Hi",
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity, stringToParty(counterParty))
        )
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val cmd = Command(SampleContract.Commands.Register(), listOf(ourIdentity.owningKey, stringToParty(counterParty).owningKey))
        addOutputState(outState())
        addCommand(cmd)
    }
}

@InitiatedBy(SendFlow::class)
class SendFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
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