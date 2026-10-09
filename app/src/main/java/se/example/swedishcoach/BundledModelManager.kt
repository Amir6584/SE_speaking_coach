package se.example.swedishcoach

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BundledModelManager(private val context: Context) {
    companion object {
        const val MODEL_LABEL = "Qwen3 0.6B Q4_0"
        const val FILE_NAME = "qwen3-0.6b-q4_0.gguf"
        private const val EXPECTED_PARTS = 16
        private const val MIN_SIZE = 400_000_000L
        private const val EXTRA_FREE_SPACE = 128L * 1024L * 1024L
    }

    suspend fun ensureModel(onStatus: (String) -> Unit = {}): File = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "models").apply {
            if (!exists() && !mkdirs()) error("Could not create model directory")
        }
        val target = File(dir, FILE_NAME)

        if (target.exists() && target.length() >= MIN_SIZE) {
            onStatus("Grammar model ready (${target.length() / (1024 * 1024)} MB)")
            return@withContext target
        }

        // Reclaim the old 135M model after an app upgrade.
        listOf(
            "swedish-smollm2-135m-cpt-q8_0.gguf",
            "swedish-smollm2-135m-cpt-q8_0.gguf.part",
        ).forEach { oldName ->
            runCatching { File(dir, oldName).takeIf { it.exists() }?.delete() }
        }

        val ids = BundledModelParts.ids
        if (ids.size != EXPECTED_PARTS) {
            error("Qwen3 model manifest is invalid: expected $EXPECTED_PARTS parts, found ${ids.size}")
        }

        try {
            context.resources.openRawResource(ids.first()).close()
        } catch (t: Throwable) {
            error("Compiled Qwen3 model part 000 could not be opened: ${t.message ?: t.javaClass.simpleName}")
        }

        if (context.filesDir.usableSpace < MIN_SIZE + EXTRA_FREE_SPACE) {
            error("Not enough free storage to install the Qwen3 grammar model")
        }

        val partFile = File(dir, "$FILE_NAME.part")
        if (partFile.exists()) partFile.delete()

        var totalCopied = 0L
        try {
            partFile.outputStream().buffered(256 * 1024).use { output ->
                ids.forEachIndexed { index, resourceId ->
                    onStatus("Installing Qwen3 grammar model (${index + 1}/$EXPECTED_PARTS)…")
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
                output.flush()
            }
        } catch (t: Throwable) {
            partFile.delete()
            error("Could not reconstruct Qwen3 GGUF after $totalCopied bytes: ${t.message ?: t.javaClass.simpleName}")
        }

        if (totalCopied < MIN_SIZE || partFile.length() < MIN_SIZE) {
            val actual = partFile.length()
            partFile.delete()
            error("Bundled Qwen3 GGUF is incomplete: copied $totalCopied bytes; file has $actual bytes")
        }

        if (target.exists()) target.delete()
        if (!partFile.renameTo(target)) {
            partFile.copyTo(target, overwrite = true)
            partFile.delete()
        }

        if (!target.exists() || target.length() < MIN_SIZE) {
            error("Qwen3 grammar model installation failed after reconstruction")
        }

        onStatus("Grammar model ready (${target.length() / (1024 * 1024)} MB)")
        target
    }
}
