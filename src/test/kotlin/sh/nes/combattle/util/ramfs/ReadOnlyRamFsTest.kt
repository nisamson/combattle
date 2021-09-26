package sh.nes.combattle.util.ramfs

import ReadOnlyRamFs
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.ByteSequence
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern

val MAIN_SCRIPT_JS = "/simple.mjs"

internal class ReadOnlyRamFsTest {

    val fs = ReadOnlyRamFs(UUID.randomUUID().toString())

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("sh.nes.combattle"))
                .setScanners(ResourcesScanner())
        )
        reflections.getResources(Pattern.compile(".*\\.mjs"))
            .forEach {
                val contents = javaClass.classLoader.getResourceAsStream(it)!!.readAllBytes()
                fs.writeFileCreatingDirs(it.removePrefix("sh/nes/combattle/scripting"), contents)
            }
    }

    @Test
    fun testSimpleRun() {
        val context = fs.createContext()
        val contents = Files.readAllBytes(fs.parsePath(MAIN_SCRIPT_JS))
        val exports = context.eval(Source.newBuilder("js", contents.decodeToString(), MAIN_SCRIPT_JS).build());
        exports.getMember("doTheThing")
    }
}
