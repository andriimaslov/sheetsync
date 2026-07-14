package dev.maslov.sheetsync.service.parser

import javax.inject.Inject

class Privat24BusinessParser @Inject constructor() : AbstractPrivat24Parser() {
    override val name: String = NAME
    override val tag: String = TAG
    override val accountType: String = ACCOUNT_TYPE

    companion object {
        private const val NAME = "PRIVAT24BUSINESS"
        private const val TAG = "Privat24BusinessParser"
        private const val ACCOUNT_TYPE = "fop"
    }
}
