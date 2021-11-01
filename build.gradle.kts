import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.grammarkit.tasks.GenerateParser
import org.jetbrains.grammarkit.tasks.*

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  idea
  // Java support
  id("java")
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.5.30"
  // Gradle IntelliJ Plugin
  id("org.jetbrains.intellij") version "1.1.6"
  // Gradle Changelog Plugin
  id("org.jetbrains.changelog") version "1.3.0"
  // Gradle Qodana Plugin
  id("org.jetbrains.qodana") version "0.1.12"

  // GrammarKit plugin
  id("org.jetbrains.grammarkit") version "2021.1.3"
}

val generatedSourcesPath = file("src/main/gen")

java.sourceSets["main"].java.srcDir(generatedSourcesPath)

idea {
  module {
    generatedSourceDirs.add(generatedSourcesPath)

  }
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
  mavenCentral()
}

dependencies {
  testImplementation("com.google.truth", "truth", "1.1.3")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  downloadSources.set(properties("platformDownloadSources").toBoolean())
  updateSinceUntilBuild.set(true)

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  version.set(properties("pluginVersion"))
  groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
  cachePath.set(projectDir.resolve(".qodana").canonicalPath)
  reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
  saveReport.set(true)
  showReport.set(System.getenv("QODANA_SHOW_REPORT").toBoolean())
}


tasks {
  // Set the JVM compatibility versions
  properties("javaVersion").let {
    val parserTasks = findFiles("bnf").map { generateParser(it) }
    val lexerTasks = findFiles("flex").map { generateLexer(it) }

    withType<JavaCompile> {
      sourceCompatibility = it
      targetCompatibility = it
    }
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = it
      dependsOn((parserTasks + lexerTasks).toTypedArray())
    }
  }

  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(
      projectDir.resolve("README.md").readText().lines().run {
        val start = "<!-- Plugin description -->"
        val end = "<!-- Plugin description end -->"

        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end))
      }.joinToString("\n").run { markdownToHTML(this) }
    )

    // Get the latest available change notes from the changelog file
    changeNotes.set(provider {
      changelog.run {
        getOrNull(properties("pluginVersion")) ?: getLatest()
      }.toHTML()
    })
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
  }

  // Configure UI tests plugin
  // Read more: https://github.com/JetBrains/intellij-ui-test-robot
  runIdeForUiTests {
    systemProperty("robot-server.port", "8082")
    systemProperty("ide.mac.message.dialogs.as.sheets", "false")
    systemProperty("jb.privacy.policy.text", "<!--999.999-->")
    systemProperty("jb.consents.confirmation.enabled", "false")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token.set(System.getenv("PUBLISH_TOKEN"))
    // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }
}

fun generateParser(source: String) = task<GenerateParser>("generateParser-${File(source).nameWithoutExtension}") {
  val srcRoot = "src/main"
  this.source = source
  targetRoot = "$srcRoot/gen"

  val srcFile = File(source)
  val grammarDir = srcFile.parent.substring("$srcRoot/kotlin/".length)
  pathToParser = "$grammarDir/${srcFile.nameWithoutExtension}Parser.java"
  pathToPsiRoot = "$grammarDir/f"
  purgeOldFiles = true
}

fun generateLexer(source: String) = task<GenerateLexer>("generateLexer-${File(source).nameWithoutExtension}") {
  val srcRoot = "src/main"
  this.source = source
  val srcFile = File(source)
  targetDir = "src/main/gen/${srcFile.parent.substring("$srcRoot/kotlin/".length)}"
  targetClass = "${srcFile.nameWithoutExtension}Lexer"
  purgeOldFiles = true
}

fun findFiles(extension: String): List<String> {
  val removePrefix = project.rootDir.path.length + 1
  return project.fileTree("src").filter { it.extension == extension }.map { it.path.substring(removePrefix) }
}
