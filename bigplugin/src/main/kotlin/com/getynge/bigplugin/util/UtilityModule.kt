package com.getynge.bigplugin.util

import com.getynge.bigplugin.MainClass
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import javax.inject.Singleton

@Module
abstract class UtilityModule {
    @Binds
    abstract fun mainClass(mainClass: MainClass): JavaPlugin

    companion object {
        @Provides
        @Singleton
        fun logger(plugin: JavaPlugin): Logger {
            return plugin.logger
        }
    }
}
