package aerotrax

import aerotrax.contracts.ProductContract
import aerotrax.functions.FlowFunctions
import aerotrax.states.ProductState
import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class RegisterProduct (private val prodName: String,
                       private val serialId: String,
                       private val ownerId: String): FlowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        return subFlow(FinalityFlow(verifyAndSign(builder()), listOf()))
    }

    private fun outState(): ProductState
    {
        val owner = inputUserRefUsingLinearID(stringToLinearID(ownerId)).state.data
        return ProductState(
                prodName = prodName,
                serialId = serialId,
                ownerId = owner.linearId,
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity)
        )
    }

    private fun builder() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val cmd = Command(ProductContract.Commands.Register(), listOf(ourIdentity.owningKey))
        addOutputState(outState(), ProductContract.PRODUCT_ID)
        addCommand(cmd)
    }
}

//@InitiatedBy(RegisterProduct::class)
//class RegisterProductResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
//{
//    @Suspendable
//    override fun call(): SignedTransaction
//    {
//        subFlow(object : SignTransactionFlow(flowSession)
//        {
//            override fun checkTransaction(stx: SignedTransaction)
//            {
//            }
//        })
//        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
//    }
//}