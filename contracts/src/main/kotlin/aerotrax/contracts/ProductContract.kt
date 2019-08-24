package aerotrax.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class ProductContract : Contract
{
    companion object
    {
        const val PRODUCT_ID = "aerotrax.contracts.ProductContract"
    }

    override fun verify(tx: LedgerTransaction)
    {

    }

    interface Commands : CommandData
    {
        class Register : TypeOnlyCommandData(), Commands
        class Remove : TypeOnlyCommandData(), Commands
        class Sell : TypeOnlyCommandData(), Commands
    }
}