package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.service.parser.NotificationParser
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ParserViewModel @Inject constructor(parserMap: Map<String, @JvmSuppressWildcards NotificationParser>) :
    ViewModel() {

    private val _parsers = MutableStateFlow(parserMap.values.toList())
    val parsers = _parsers.asStateFlow()
}
