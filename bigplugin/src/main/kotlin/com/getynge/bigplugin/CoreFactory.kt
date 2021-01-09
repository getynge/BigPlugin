package com.getynge.bigplugin

import com.getynge.bigplugin.listeners.ListenerModule
import com.getynge.bigplugin.util.UtilityModule
import com.getynge.bigplugin.commands.GeneratedCommandsModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UtilityModule::class, ListenerModule::class, GeneratedCommandsModule::class] )
interface CoreFactory {
    fun inject(mainClass: MainClass)

    @Component.Builder
    interface Builder {
        fun build(): CoreFactory
        @BindsInstance fun mainClass(mainClass: MainClass): Builder
    }
}
