package corexchange.states

import com.r3.corda.lib.tokens.contracts.types.TokenType
import corexchange.contracts.UserContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(UserContract::class)
data class UserState(val name: String,
                     val wallet: List<Amount<TokenType>>,
                     override val participants: List<Party>,
                     override val linearId: UniqueIdentifier): LinearState

@CordaSerializable
data class CorexToken(override val tokenIdentifier: String,
                      override val fractionDigits: Int): TokenType(tokenIdentifier, fractionDigits)
