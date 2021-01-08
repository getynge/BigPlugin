package com.getynge.pymlg

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Plugin(val name: String, val version: String)

// FIXME: this works, but I'm pretty sure there is a way to process repetition without using java's repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@java.lang.annotation.Repeatable(PluginPermissions::class)
@Repeatable
annotation class PluginPermission(val name: String, val description: String = "", val default: String = "true")

@Retention(AnnotationRetention.SOURCE)
annotation class PluginPermissions(val value: Array<PluginPermission>)
