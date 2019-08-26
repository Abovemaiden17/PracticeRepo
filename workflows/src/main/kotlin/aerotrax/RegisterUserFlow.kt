package aerotrax

import aerotrax.contracts.UserContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.UserState
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
class RegisterUserFlow (private val name: String,
                        private val companyName: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val session = initiateFlow(stringToParty("PartyB"))
        val stx = subFlow(CollectSignaturesFlow(verifyAndSign(builder()), listOf(session)))
        return subFlow(FinalityFlow(stx, listOf(session)))
    }

    private fun outState(): UserState
    {
        val partyB = stringToParty("PartyB")
        return UserState(
                name = name,
                companyName = companyName,
                wallet = Amount.fromDecimal(100.toBigDecimal(), Currency.getInstance("USD")),
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity, partyB)
        )
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val partyB = stringToParty("PartyB")
        val cmd = Command(UserContract.Commands.Register(), listOf(ourIdentity.owningKey, partyB.owningKey))
        addOutputState(outState(), UserContract.USER_ID)
        addCommand(cmd)
    }
}

@InitiatedBy(RegisterUserFlow::class)
class RegisterUserFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
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