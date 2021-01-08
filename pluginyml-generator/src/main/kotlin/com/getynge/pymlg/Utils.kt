package com.getynge.pymlg

import java.lang.Exception

class AnnotationException(message: String): Exception(message)

data class Command(val name: String, val usage: String, val description: String, val permission: String)
data class PermissionInfo(val name: String, val description: String, val default: String)
data class PluginInfo(val main: String, val name: String, val version: String)
