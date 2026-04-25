package dev.maslov.sheetsync.service

import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.dao.RuleDao

class RuleRepository(private val dao: RuleDao) {
    val rules = dao.getAllRules()

    suspend fun addRule(rule: Rule) = dao.insertRule(rule)

    suspend fun updateRule(rule: Rule) = dao.updateRule(rule)

    suspend fun deleteRule(rule: Rule) = dao.deleteRule(rule)
}
