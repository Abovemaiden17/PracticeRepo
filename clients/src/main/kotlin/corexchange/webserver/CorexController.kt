package corexchange.webserver

import com.fasterxml.jackson.databind.SerializationFeature
import com.r3.corda.lib.tokens.contracts.types.TokenType
import corexchange.flows.RegisterFlow
import com.template.models.CorexRegisterModel
import com.template.models.CorexUserModel
import corexchange.states.UserState
import corexchange.webserver.utilities.FlowHandlerCompletion
import corexchange.webserver.utilities.Plugin
import net.corda.core.contracts.Amount
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("corex")
class CorexController(rpc: NodeRPCConnection, private val flowHandlerCompletion: FlowHandlerCompletion, private val plugin: Plugin)
{
    companion object
    {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    /**
     * Return all users
     */
    @GetMapping(value = ["states/users"], produces = ["application/json"])
    private fun getAllUsers(): ResponseEntity<Map<String, Any>>
    {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val infoStateRef = proxy.vaultQueryBy<UserState>().states
            val infoStates = infoStateRef.map { it.state.data }
            val list = infoStates.map {
                CorexUserModel(
                        name = it.name,
                        wallet = it.wallet
                )
            }
            HttpStatus.CREATED to list
        }
        catch (e: Exception)
        {
            logger.info(e.message)
            HttpStatus.BAD_REQUEST to "No fungible state found."
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "message" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }

    /**
     * Register a user account
     */
    @PostMapping(value = ["register/user"], produces = ["application/json"])
    private fun registerUser(@RequestBody registerModel: CorexRegisterModel): ResponseEntity<Map<String, Any>>
    {
        val (status, result) = try {
            val register = CorexRegisterModel(
                    name = registerModel.name,
                    amount = registerModel.amount,
                    token = registerModel.token
            )
            val flowReturn = proxy.startFlowDynamic(
                    RegisterFlow::class.java,
                    register.name,
                    listOf(Amount(register.amount, TokenType(register.token.tokenIdentifier, register.token.fractionDigits)))
            )
            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to registerModel
        }
        catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "message" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }
        val res = "result" to result

        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }
}