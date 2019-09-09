package sample.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class SampleContract : Contract
{
    companion object
    {
        const val SAMPLE_ID = "sample.contracts.SampleContract"
    }

    override fun verify(tx: LedgerTransaction)
    {

    }

    interface Commands : CommandData
    {
        class Register : TypeOnlyCommandData(), Commands
    }
}