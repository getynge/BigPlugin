package com.getynge.bigplugin.commands

import com.getynge.pymlg.Description
import com.getynge.pymlg.Permission
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.bukkit.command.CommandExecutor

@Module
abstract class CommandModule {
    @Binds
    @IntoMap
    @StringKey("spondoolie")
    @Permission("bigplugin.utils")
    @Description("gives the player 100 levels")
    abstract fun spondoolie(spondoolie: Spondoolie): CommandExecutor

    @Binds
    @IntoMap
    @StringKey("feed")
    @Permission("bigplugin.utils")
    @Description("refills hunger meter")
    abstract fun feed(feed: Feed): CommandExecutor
}
