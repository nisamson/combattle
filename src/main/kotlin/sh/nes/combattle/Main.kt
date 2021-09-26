package sh.nes.combattle

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.charleskorn.kaml.Yaml
import kotlinx.cli.*
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import ktx.app.KtxApplicationAdapter
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.freetype.async.registerFreeTypeFontLoaders
import org.graalvm.polyglot.Context
import java.nio.file.Path

@ExperimentalSerializationApi
fun main(args: Array<String>) {
    val parser = ArgParser("combattle")
    val debug by parser.option(ArgType.Boolean, shortName = "d", description = "Enable debug mode").default(false)
    val gamePath by parser.argument(ArgType.String, description = "Game folder path")
    parser.parse(args)

//    val manifestPath = Path.of(gamePath, "manifest.yaml")
//    val maniFile = manifestPath.toFile()
//    val manifest = Yaml.default.decodeFromStream(Manifest.serializer(), maniFile.inputStream())
//
//    val config = LwjglApplicationConfiguration()
//    config.width = 256
//    config.height = 192
//    config.foregroundFPS = 30
//    config.resizable = false
//
//    config.title = manifest.title
//    val game = Wrightful(manifest)
//    LwjglApplication(game, config)
}