package corexchange.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import corexchange.*
import corexchange.contracts.UserContract
import corexchange.functions.CorexFunctions
import corexchange.states.CorexToken
import corexchange.states.UserState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowSession
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class RegisterFlow (private val name: String,
                    private val amount: Long,
                    private val token: CorexToken): CorexFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        progressTracker.currentStep = CREATING
        val registration = register()

        progressTracker.currentStep = VERIFYING
        progressTracker.currentStep = SIGNING
        val signedTransaction = verifyAndSign(registration)
        val sessions = emptyList<FlowSession>()
        val transactionSignedByParties = subFlow(CollectSignaturesFlow(signedTransaction, sessions))

        progressTracker.currentStep = NOTARIZING
        progressTracker.currentStep = FINALIZING
        return subFlow(FinalityFlow(transactionSignedByParties, sessions))
    }

    private fun outState(): UserState
    {
        return UserState(
                name = name,
                wallet = listOf(Amount(amount, TokenType(token.tokenIdentifier, token.fractionDigits))),
                participants = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }

    private fun register() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val cmd = Command(UserContract.Commands.Register(), ourIdentity.owningKey)
        addOutputState(outState(), UserContract.contractId)
        addCommand(cmd)
    }
}