package se.example.swedishcoach

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BundledModelManager(private val context: Context) {
    companion object {
        const val MODEL_LABEL = "Swedish SmolLM2 135M CPT Q8_0"
        const val FILE_NAME = "swedish-smollm2-135m-cpt-q8_0.gguf"
        private const val MIN_SIZE = 120L * 1024L * 1024L
        private const val EXTRA_FREE_SPACE = 40L * 1024L * 1024L
    }

    suspend fun ensureModel(onStatus: (String) -> Unit = {}): File = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "models").apply { if (!exists() && !mkdirs()) error("Could not create model directory") }
        val target = File(dir, FILE_NAME)
        if (target.exists() && target.length() >= MIN_SIZE) {
            onStatus("Grammar model ready (${target.length() / (1024 * 1024)} MB)")
            return@withContext target
        }

        val ids = BundledModelParts.ids
        if (ids.size != 8) error("Swedish grammar model manifest is invalid: expected 8 parts, found ${ids.size}")
        try { context.resources.openRawResource(ids.first()).close() }
        catch (t: Throwable) { error("Compiled Swedish model part 000 could not be opened: ${t.message ?: t.javaClass.simpleName}") }

        if (context.filesDir.usableSpace < MIN_SIZE + EXTRA_FREE_SPACE) error("Not enough free storage to install Swedish grammar model")

        val partFile = File(dir, "$FILE_NAME.part")
        if (partFile.exists()) partFile.delete()
        var totalCopied = 0L
        try {
            partFile.outputStream().buffered(256 * 1024).use { output ->
                ids.forEachIndexed { index, resourceId ->
                    onStatus("Installing Swedish grammar model (${index + 1}/${ids.size})…")
                    context.resources.openRawResource(resourceId).use { input ->
                        val buffer = ByteArray(256 * 1024)
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            output.write(buffer, 0, n)
                            totalCopied += n
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            partFile.delete()
            error("Could not reconstruct Swedish GGUF after $totalCopied bytes: ${t.message ?: t.javaClass.simpleName}")
        }
        if (totalCopied < MIN_SIZE || partFile.length() < MIN_SIZE) { partFile.delete(); error("Bundled Swedish GGUF is incomplete") }
        if (target.exists()) target.delete()
        if (!partFile.renameTo(target)) { partFile.copyTo(target, overwrite = true); partFile.delete() }
        onStatus("Grammar model ready (${target.length() / (1024 * 1024)} MB)")
        target
    }
}
