package com.getynge.bigplugin.commands

import com.getynge.pymlg.PluginCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@PluginCommand(
    name = "pick",
    description = "picks between one of at least two things",
    usage = "/<command> thing1 thing2 [more things...]",
    permission = "bigplugin.playerutils"
)
class Pick @Inject constructor(): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val distinctArgs = args.distinct()
        if(distinctArgs.size < 2) {
            sender.sendMessage("this command requires at least 2 distinct arguments!")
            return false
        }

        sender.sendMessage(distinctArgs.random())

        return true
    }
}
