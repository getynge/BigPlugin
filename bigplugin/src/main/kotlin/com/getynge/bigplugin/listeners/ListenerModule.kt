package com.getynge.bigplugin.listeners

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.bukkit.event.Listener

@Module
abstract class ListenerModule {
    @Binds
    @IntoSet
    abstract fun playerInitializationListener(playerInitializationListener: PlayerInitializationListener): Listener
}
