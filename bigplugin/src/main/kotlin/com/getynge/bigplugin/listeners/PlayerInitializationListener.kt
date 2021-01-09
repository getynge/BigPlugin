package com.getynge.bigplugin.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerInitializationListener @Inject constructor(): Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("Welcome ${event.player.name}")
    }
}
