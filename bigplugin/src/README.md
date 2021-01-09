# BigPlugin
## Structure of this project
All sources are in `src/main/kotlin` and the base package is `com.getynge.bigplugin`.
All packages and classes mentioned from here on will be relative to `com.getynge.bigplugin`.

`MainClass` is the entrypoint of the plugin, and where dependency injection is set up.

`CoreFactory` is the main dagger component for this plugin

Each package may have a dagger module for use with classes in that package.
These modules are all applied to CoreFactory.
