package com.getynge.bigplugin.util

import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerUtils @Inject constructor(val plugin: JavaPlugin) {
    fun setMetaString(player: Player, key: String, value: String) {
        player.setMetadata(key, FixedMetadataValue(plugin, value))
    }

    fun getMetaString(player: Player, key: String): String {
        return player.getMetadata(key)[0].asString()
    }
}
