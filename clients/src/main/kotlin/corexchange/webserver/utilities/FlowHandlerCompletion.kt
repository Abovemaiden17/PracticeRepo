package corexchange.webserver.utilities

import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import org.springframework.stereotype.Service

import java.util.*

@Service
class FlowHandlerCompletion
{
    fun flowHandlerCompletion(flowReturn: FlowHandle<SignedTransaction>)
    {
        listOf(flowReturn).forEach { test -> test.returnValue.get() }
    }
}