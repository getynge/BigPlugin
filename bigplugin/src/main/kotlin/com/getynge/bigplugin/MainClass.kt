package com.getynge.bigplugin

import com.getynge.pymlg.Plugin
import com.getynge.pymlg.PluginPermission
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject
import javax.inject.Singleton

@JvmSuppressWildcards
@Singleton
@Plugin(name = "BigPlugin", version = "0.1")
@PluginPermission(name = "bigplugin.utils", description = "gives access to utility commands", default = "op")
class MainClass: JavaPlugin() {
    @Inject lateinit var commands: Map<String, CommandExecutor>
    @Inject lateinit var listeners: Set<Listener>

    init {
        val coreFactory = DaggerCoreFactory.builder().mainClass(this).build()
        coreFactory.inject(this)
    }

    override fun onEnable() {
        for(command in commands.keys) {
            getCommand(command)?.setExecutor(commands[command]) ?: run {
                logger.warning("attempted to register nonexistent command $command")
            }
        }

        for(listener in listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this@MainClass)
        }

        logger.info("all commands registered")
    }
}
