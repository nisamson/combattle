import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.soywiz.korio.lang.substr
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.io.FileSystem
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import kotlin.io.NoSuchFileException

class ReadOnlyRamFs(val name: String) : FileSystem {
    val vfs = Jimfs.newFileSystem(name, Configuration.unix().toBuilder().setWorkingDirectory("/").build())

    /**
     * Parses a path from an [URI].
     *
     * @param uri the [URI] to be converted to [Path]
     * @return the [Path] representing given [URI]
     * @throws UnsupportedOperationException when [URI] scheme is not supported
     * @since 19.0
     */
    override fun parsePath(uri: URI?): Path {
        uri!!
        if (uri.scheme == "file") {
            val base = uri.path
            return vfs.getPath(base)
        } else {
            throw UnsupportedOperationException("Scheme ${uri.scheme} is not supported")
        }
    }

    fun getJimURI(base: String): URI {
        var base = base
        if (base.startsWith("/")) {
            base = base.substr(1)
        }

        return URI.create("jimfs://$name/$base")
    }

    fun createContext(): Context {
        return Context.newBuilder()
            .allowIO(true)
            .fileSystem(this)
            .allowExperimentalOptions(true)
            .option("js.esm-eval-returns-exports", "true")
            .build()
    }

    fun writeFile(path: String, contents: ByteArray) {
        val realPath = this.parsePath(path)
        Files.write(realPath, contents)
    }

    fun writeFileCreatingDirs(path: String, contents: ByteArray) {
        val realPath = this.parsePath(path)
        Files.createDirectories(realPath.parent)
        Files.write(realPath, contents)
    }

    fun readFile(path: String): ByteArray {
        return Files.readAllBytes(this.parsePath(path))
    }

    /**
     * Parses a path from a [String]. This method is called only on the [FileSystem]
     * with `file` scheme.
     *
     * @param path the string path to be converted to [Path]
     * @return the [Path]
     * @throws UnsupportedOperationException when the [FileSystem] supports only [URI]
     * @since 19.0
     */
    override fun parsePath(path: String?): Path {
        return this.vfs.getPath(path!!)
    }

    /**
     * Checks existence and accessibility of a file.
     *
     * @param path the path to the file to check
     * @param modes the access modes to check, possibly empty to check existence only.
     * @param linkOptions options determining how the symbolic links should be handled
     * @throws NoSuchFileException if the file denoted by the path does not exist
     * @throws IOException in case of IO error
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun checkAccess(path: Path?, modes: MutableSet<out AccessMode>?, vararg linkOptions: LinkOption?) {
        if (modes!!.any {
            accessMode -> accessMode == AccessMode.WRITE
        }) {
            throw SecurityException()
        }

        vfs.provider().checkAccess(path ?: throw NoSuchFileException("<null>"))
    }

    /**
     * Creates a directory.
     *
     * @param dir the directory to create
     * @param attrs the optional attributes to set atomically when creating the directory
     * @throws FileAlreadyExistsException if a file on given path already exists
     * @throws IOException in case of IO error
     * @throws UnsupportedOperationException if the attributes contain an attribute which cannot be
     * set atomically
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) {
        throw SecurityException("Cannot write to system")
    }

    /**
     * Deletes a file.
     *
     * @param path the path to the file to delete
     * @throws NoSuchFileException if a file on given path does not exist
     * @throws DirectoryNotEmptyException if the path denotes a non empty directory
     * @throws IOException in case of IO error
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun delete(path: Path?) {
        throw SecurityException("Cannot write to system")
    }

    /**
     * Opens or creates a file returning a [SeekableByteChannel] to access the file content.
     *
     * @param path the path to the file to open
     * @param options the options specifying how the file should be opened
     * @param attrs the optional attributes to set atomically when creating the new file
     * @return the created [SeekableByteChannel]
     * @throws FileAlreadyExistsException if [StandardOpenOption.CREATE_NEW] option is set and
     * a file already exists on given path
     * @throws IOException in case of IO error
     * @throws UnsupportedOperationException if the attributes contain an attribute which cannot be
     * set atomically
     * @throws IllegalArgumentException in case of invalid options combination
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun newByteChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel {
        return Files.newByteChannel(path!!, options!!, *attrs)
    }

    /**
     * Returns directory entries.
     *
     * @param dir the path to the directory to iterate entries for
     * @param filter the filter
     * @return the new [DirectoryStream]
     * @throws NotDirectoryException when given path does not denote a directory
     * @throws IOException in case of IO error
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
        return Files.newDirectoryStream(dir!!, filter!!)
    }

    /**
     * Resolves given path to an absolute path.
     *
     * @param path the path to resolve, may be a non normalized path
     * @return an absolute [Path]
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun toAbsolutePath(path: Path?): Path {
        return path!!.toAbsolutePath()
    }

    /**
     * Returns the real (canonical) path of an existing file.
     *
     * @param path the path to resolve, may be a non normalized path
     * @param linkOptions options determining how the symbolic links should be handled
     * @return an absolute canonical path
     * @throws IOException in case of IO error
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun toRealPath(path: Path?, vararg linkOptions: LinkOption?): Path {
        return path!!.toRealPath(*linkOptions)
    }

    /**
     * Reads a file's attributes as a bulk operation.
     *
     * @param path the path to file to read attributes for
     * @param attributes the attributes to read. The `attributes` parameter has the form:
     * `[view-name:]attribute-list`. The optional `view-name` corresponds to
     * [FileAttributeView.name] and determines the set of attributes, the default
     * value is `"basic"`. The `attribute-list` is a comma separated list of
     * attributes. If the `attribute-list` contains `'*'` then all the
     * attributes from given view are read.
     * @param options the options determining how the symbolic links should be handled
     * @return the [Map] containing the file attributes. The map's keys are attribute names,
     * map's values are the attribute values. The map may contain a subset of required
     * attributes in case when the `FileSystem` does not support some of the required
     * attributes.
     * @throws UnsupportedOperationException if the attribute view is not supported. At least the
     * `"basic"` attribute view has to be supported by the file system.
     * @throws IllegalArgumentException is the `attribute-list` is empty or contains an
     * unknown attribute
     * @throws IOException in case of IO error
     * @throws SecurityException if this [FileSystem] denied the operation
     * @since 19.0
     */
    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> {
        return Files.readAttributes(path!!, attributes!!, *options)
    }
}