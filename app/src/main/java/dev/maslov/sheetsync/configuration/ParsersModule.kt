package dev.maslov.sheetsync.configuration

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import dev.maslov.sheetsync.service.parser.NotificationParser
import dev.maslov.sheetsync.service.parser.Privat24BusinessParser
import dev.maslov.sheetsync.service.parser.Privat24Parser

@Module
@InstallIn(SingletonComponent::class)
interface ParsersModule {

    @IntoMap
    @StringKey("PRIVAT24")
    @Binds
    fun privat24Parser(parser: Privat24Parser): NotificationParser

    @IntoMap
    @StringKey("PRIVAT24BUSINESS")
    @Binds
    fun privat24BusinessParser(parser: Privat24BusinessParser): NotificationParser
}
