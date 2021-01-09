package com.getynge.pymlg

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class PluginCommand(
    val name: String,
    val description: String = "",
    val usage: String = "/<command>",
    val permission: String = ""
)
