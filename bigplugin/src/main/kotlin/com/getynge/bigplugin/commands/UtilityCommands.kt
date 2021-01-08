package com.getynge.bigplugin.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

private const val PLAYER_ONLY_MESSAGE = "this command only works for players!"

@Singleton
class Spondoolie @Inject constructor(): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage(PLAYER_ONLY_MESSAGE)
            return false
        }
        sender.giveExpLevels(100)
        return true
    }
}

@Singleton
class Feed @Inject constructor(): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage(PLAYER_ONLY_MESSAGE)
            return false
        }

        sender.foodLevel = 20
        sender.saturation = 20.0f

        return true
    }
}
