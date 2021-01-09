package com.getynge.pymlg

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.bukkit.command.CommandExecutor
import java.lang.Exception
import java.lang.reflect.Type
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedAnnotationTypes("com.getynge.pymlg.*")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
class AnnotationProcessor: AbstractProcessor() {
    private var processedPlugin: PluginInfo? = null
    private var commands = arrayListOf<Command>()
    private var commandPackage = ""
    private var javaWritten = false
    private var permissions = arrayListOf<PermissionInfo>()

    private lateinit var elementUtils: Elements
    private lateinit var typeUtils: Types
    private lateinit var filer: Filer
    private lateinit var messager: Messager

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
        } catch(e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to process annotations due to error: ${e.message}")
        }

        writeJava()

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

        for(element in roundEnv.getElementsAnnotatedWith(PluginCommand::class.java)) {
            var name: String
            var description: String
            var usage: String
            var permission: String
            var packageName: String
            var fullPackageName: String

            val pluginAnnotation = element.getAnnotation(PluginCommand::class.java)

            if(element.kind != ElementKind.CLASS || element.modifiers.contains(Modifier.ABSTRACT)) {
                continue
            }

            messager.printMessage(Diagnostic.Kind.NOTE, "passed first filter")

            val typeElement = element as TypeElement

            if(typeElement.interfaces.filter { it.toString() != CommandExecutor::class.java.toString().split(" ")[1] }
                    .isNotEmpty()) {
                continue
            }


            messager.printMessage(Diagnostic.Kind.NOTE, "passed second filter")

            var enclosing = element
            while(enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            packageName = enclosing.simpleName.toString()
            fullPackageName = enclosing.toString()

            name = pluginAnnotation.name
            description = pluginAnnotation.description
            usage = pluginAnnotation.usage
            permission = if(pluginAnnotation.permission != "") pluginAnnotation.permission else "$packageName.$name"

            val command =  Command(fullPackageName, typeElement.simpleName.toString(), name, usage, description, permission)
            messager.printMessage(Diagnostic.Kind.NOTE, "found command: $command")

            if(commandPackage == "") {
                commandPackage = fullPackageName
            }

            if(commandPackage != "" && commandPackage != fullPackageName) {
                throw AnnotationException("you may not have commands in more than one package")
            }

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

    // TODO: update writer to support commands in multiple packages
    private fun writeJava() {
        if(commandPackage == "" || javaWritten) {
            return
        }

        val re = Regex("[^A-Za-z]")
        val name = "${commandPackage.split(".").last().capitalize()}Module"

        val typeSpecBuilder = TypeSpec.classBuilder(name)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(Module::class.java)

        for(command in commands) {
            val sanitizedName = re.replace(command.name, "")

            val annotationSpec = AnnotationSpec.builder(StringKey::class.java)
                .addMember("value", "\"${command.name}\"")
                .build()

            val methodSpec = MethodSpec.methodBuilder(sanitizedName)
                .addAnnotation(Binds::class.java)
                .addAnnotation(IntoMap::class.java)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.ABSTRACT)
                .returns(ClassName.get(CommandExecutor::class.java))
                .addParameter(ClassName.get(command.pkg, command.type), sanitizedName)
                .build()

            typeSpecBuilder.addMethod(methodSpec)
        }

        val typeSpec = typeSpecBuilder.build()

        val javaFile = JavaFile.builder(commandPackage, typeSpec).build()

        javaFile.writeTo(filer)

        javaWritten = true
    }
}
