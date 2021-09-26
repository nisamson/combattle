package sh.nes.combattle.assets

import ReadOnlyRamFs
import com.charleskorn.kaml.Yaml
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.file.std.openAsZip
import com.soywiz.korio.file.useVfs
import com.soywiz.korio.lang.InvalidArgumentException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ktx.assets.async.AssetStorage
import org.graalvm.polyglot.Context
import java.util.*


@Serializable
class Faction(val name: String, val description: String, val cards: Array<Card>) {
    init {
        if (cards.sumOf { it.numInDeck } != 20u) {
            throw IllegalArgumentException("Must be exactly 20 cards in a faction")
        }
    }
}

@Serializable
class ResourcePackManifest(val name: String, val factions: Array<Faction>) {
    companion object {
        suspend fun loadFromZipPath(path: String): ResourcePackManifest {
            val manifestBytes = localVfs(path).openAsZip(true).useVfs {
                it["manifest.yaml"].readAll()
            }
            return Yaml.default.decodeFromStream(ResourcePackManifest.serializer(), manifestBytes.inputStream())
        }

        suspend fun loadFromDirectory(path: String): ResourcePackManifest {
            val manifestBytes = localVfs(path)["manifest.yaml"].readAll()
            return Yaml.default.decodeFromStream(serializer(), manifestBytes.inputStream())
        }

        suspend fun loadFromPath(path: String): ResourcePackManifest {
            return if (path.endsWith(".zip")) {
                loadFromZipPath(path)
            } else {
                loadFromDirectory(path)
            }
        }
    }
}

@Serializable(with = CardType.CardTypeSerializer::class)
enum class CardType {
    MONSTER,
    ACTION;

    object CardTypeSerializer : KSerializer<CardType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): CardType {
            return valueOf(decoder.decodeString().uppercase())
        }

        override fun serialize(encoder: Encoder, value: CardType) {
            encoder.encodeString(value.name.lowercase())
        }
    }
}

@Serializable
data class Card(val name: String,
                val basePower: UInt,
                val type: CardType,
                val script: String? = null,
                val description: String = "",
                val numInDeck: UInt = 1u) {
    init {
        if (numInDeck == 0u) {
            throw InvalidArgumentException("Can't have zero of a card in the deck")
        }

        if (numInDeck > 20u) {
            throw InvalidArgumentException("Can't have more than 20 of a single type of card in the deck")
        }
    }
}

class NameConflictError(name: String) : RuntimeException("$name was specified multiple times")

class Storage {
    private val backingFactions = mutableMapOf<String, Faction>()
    val factions: Map<String, Faction> get() = backingFactions
    private val fs: ReadOnlyRamFs = ReadOnlyRamFs(UUID.randomUUID().toString())
    val storage: AssetStorage = AssetStorage()
    private val context: Context = fs.createContext()

    fun loadFaction()
}
