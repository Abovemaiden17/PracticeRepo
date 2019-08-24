package aerotrax

import aerotrax.contracts.UserContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.UserState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

@StartableByRPC
class RegisterUserFlow (private val name: String,
                        private val companyName: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        return subFlow(FinalityFlow(verifyAndSign(builder()), listOf()))
    }

    private fun outState(): UserState
    {
        return UserState(
                name = name,
                companyName = companyName,
                wallet = Amount.parseCurrency("$100"),
//                wallet = Amount(100, Currency.getInstance("USD")),
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity)
        )
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val cmd = Command(UserContract.Commands.Register(), listOf(ourIdentity.owningKey))
        addOutputState(outState(), UserContract.USER_ID)
        addCommand(cmd)
    }
}