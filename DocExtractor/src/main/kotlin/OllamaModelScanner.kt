import java.io.File

/**
 * A utility object to scan for available Ollama models on the local machine.
 */
object OllamaModelScanner {

    /**
     * Scans the default Ollama manifests directory to find installed models and their tags.
     * @return A list of model strings in the format "model:tag" (e.g., "llama3:latest").
     */
    fun getAvailableModels(): List<String> {
        val userHome = System.getProperty("user.home")
        // Default path for Ollama model manifests
        val manifestsDir = File(userHome, ".ollama/models/manifests/registry.ollama.ai/library")

        if (!manifestsDir.exists() || !manifestsDir.isDirectory) {
            return emptyList()
        }

        // Models are directories, tags are files within those directories
        return manifestsDir.listFiles { file -> file.isDirectory }
            ?.flatMap { modelDir -> modelDir.listFiles()?.map { tagFile -> "${modelDir.name}:${tagFile.name}" } ?: emptyList() }
            ?: emptyList()
    }
}