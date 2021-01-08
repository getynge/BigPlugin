package com.getynge.pymlg

import com.google.auto.service.AutoService
import dagger.multibindings.StringKey
import org.bukkit.command.CommandExecutor
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedAnnotationTypes("com.getynge.pymlg.*", "dagger.multibindings.StringKey")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
class AnnotationProcessor: AbstractProcessor() {
    private var processedPlugin: PluginInfo? = null
    private var commands = arrayListOf<Command>()
    private var permissions = arrayListOf<PermissionInfo>()

    lateinit var elementUtils: Elements
    lateinit var typeUtils: Types
    lateinit var filer: Filer
    lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if(roundEnv.processingOver()) {
            if(processedPlugin == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "No Plugin annotation found")
            }

            writePluginyml()

            return false
        }

        try {
            doProcess(annotations, roundEnv)
        } catch(e: AnnotationException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to process annotations due to error: ${e.message}")
        }

        return false
    }

    private fun doProcess(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment) {
        for(element in roundEnv.getElementsAnnotatedWith(Plugin::class.java)) {
            if(processedPlugin != null) {
                throw AnnotationException("You cannot have more than one class annotated with @Plugin")
            }
            var main: String
            var name: String
            var version: String

            val plugin = element.getAnnotation(Plugin::class.java)

            var enclosing = element
            while(enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            main = "${enclosing}.${element.simpleName}"
            name = plugin.name
            version = plugin.version

            val pluginInfo = PluginInfo(main, name, version)

            messager.printMessage(Diagnostic.Kind.NOTE, "found plugin: $pluginInfo")

            processedPlugin = pluginInfo
        }

        for(element in roundEnv.getElementsAnnotatedWith(StringKey::class.java)) {
            var name: String
            var description: String
            var usage: String
            var permission: String
            var packageName: String

            val nameAnnotation = element.getAnnotation(StringKey::class.java)
            val descriptionAnnotation = element.getAnnotation(Description::class.java)
            val usageAnnotation = element.getAnnotation(Usage::class.java)
            val permissionAnnotation = element.getAnnotation(Permission::class.java)

            if(element.kind != ElementKind.METHOD) {
                continue
            }

            val executableElement = element as ExecutableElement

            // TODO: figure out if there's a less horrible way to do this
            if(executableElement.returnType.toString() != CommandExecutor::class.java.toString().split(" ")[1]){
                continue
            }

            var enclosing = element
            while(enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            packageName = enclosing.simpleName.toString()

            name = nameAnnotation.value
            description = descriptionAnnotation?.description ?: name
            usage = usageAnnotation?.usage ?: "/<command>"
            permission = permissionAnnotation?.name ?: "${packageName}.${name}"

            val command = Command(name, usage, description, permission)
            messager.printMessage(Diagnostic.Kind.NOTE, "found command: $command")

            commands.add(command)
        }

        for(element in roundEnv.getElementsAnnotatedWith(PluginPermission::class.java)) {
            if(element.kind != ElementKind.CLASS) {
                throw AnnotationException("@PluginPermission may only be used on classes")
            }

            if(element.getAnnotation(Plugin::class.java) == null) {
                throw AnnotationException("@PluginPermission may only be used on classes annotated with @Plugin")
            }

            val annotation = element.getAnnotation(PluginPermission::class.java)!!

            processPermission(annotation)
        }

        for(element in roundEnv.getElementsAnnotatedWith(PluginPermissions::class.java)) {
            if(element.kind != ElementKind.CLASS) {
                throw AnnotationException("@PluginPermission may only be used on classes")
            }

            if(element.getAnnotation(Plugin::class.java) == null) {
                throw AnnotationException("@PluginPermission may only be used on classes annotated with @Plugin")
            }

            val annotation = element.getAnnotation(PluginPermissions::class.java)!!

            for(child in annotation.value) {
                processPermission(child)
            }
        }
    }

    private fun processPermission(permission: PluginPermission) {
        val permissionInfo = PermissionInfo(permission.name, permission.description, permission.default)

        messager.printMessage(Diagnostic.Kind.NOTE, "found permission: $permissionInfo")

        permissions.add(permissionInfo)
    }

    private fun writePluginyml() {
        val pluginOut = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml")
        val writer = pluginOut.openWriter()

        writer.write("""
            main: ${processedPlugin!!.main}
            name: ${processedPlugin!!.name}
            version: ${processedPlugin!!.version}
            
        """.trimIndent())

        if(commands.size > 0) {
            writer.write("commands:\n")
        }

        for(command in commands) {
            writer.write("""    ${command.name}:
        description: ${command.description}
        usage: ${command.usage}
        permission: ${command.permission}
""")
        }

        if(permissions.size > 0) {
            writer.write("permissions:\n")
        }

        for(permission in permissions) {
            writer.write("""    ${permission.name}:
        description: ${permission.description}
        default: ${permission.default}
""")
        }

        writer.close()
    }
}
