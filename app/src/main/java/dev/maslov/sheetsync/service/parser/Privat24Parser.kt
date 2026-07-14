package dev.maslov.sheetsync.service.parser

import javax.inject.Inject

class Privat24Parser @Inject constructor() : AbstractPrivat24Parser() {
    override val name: String = NAME
    override val tag: String = TAG
    override val accountType: String = ACCOUNT_TYPE

    companion object {
        private const val NAME = "PRIVAT24"
        private const val TAG = "Privat24Parser"
        private const val ACCOUNT_TYPE = "fiz"
    }
}
