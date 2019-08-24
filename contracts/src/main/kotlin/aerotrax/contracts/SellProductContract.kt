package aerotrax.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class SellProductContract : Contract
{
    companion object
    {
        const val SELL_ID = "aerotrax.contracts.SellProductContract"
    }

    override fun verify(tx: LedgerTransaction)
    {

    }

    interface Commands : CommandData
    {
        class List() : TypeOnlyCommandData(), Commands
    }
}