package com.getynge.pymlg

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Plugin(val name: String, val version: String)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class PluginPermission(val name: String, val description: String = "", val default: String = "true")
