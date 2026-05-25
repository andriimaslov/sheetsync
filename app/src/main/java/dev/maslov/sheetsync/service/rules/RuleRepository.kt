package dev.maslov.sheetsync.service.rules

import dev.maslov.sheetsync.model.Rule
import java.util.UUID

class RuleRepository(private val dao: RuleDao) {
    val rules = dao.getAllRules()

    suspend fun addRule(rule: Rule) = dao.insertRule(rule)

    suspend fun updateRule(rule: Rule) = dao.updateRule(rule)

    suspend fun deleteRule(rule: Rule) = dao.deleteRule(rule)

    suspend fun getRuleById(id: UUID): Rule = dao.getRuleById(id)
}
