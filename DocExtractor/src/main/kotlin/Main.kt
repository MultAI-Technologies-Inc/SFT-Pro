import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.swing.JFileChooser
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        // GUI Mode
        application {
            Window(onCloseRequest = ::exitApplication, title = "SFT Pro Doc Extractor") {
                App()
            }
        }
    } else {
        // CLI Mode
        val parser = ArgParser("SFT-Pro-CLI")
        val input by parser.option(ArgType.String, shortName = "i", fullName = "input", description = "Input document file (PDF, DOCX)")
        val output by parser.option(ArgType.String, shortName = "o", fullName = "output", description = "Output JSONL file path")
        var model by parser.option(ArgType.String, shortName = "m", fullName = "model", description = "Ollama model to use")
        parser.parse(args)

        val inputFile = input?.let { File(it) }
        if (inputFile == null || !inputFile.exists()) {
            println("Error: Input file '-i' not specified or does not exist.")
            return
        }

        if (model == null) {
            val availableModels = OllamaModelScanner.getAvailableModels()
            when {
                availableModels.isEmpty() -> {
                    println("Error: No Ollama models found. Please install a model first (e.g., 'ollama pull llama3').")
                    return
                }
                availableModels.size == 1 -> {
                    model = availableModels.first()
                    println("Found one model, using '$model' by default.")
                }
                else -> {
                    println("Multiple Ollama models found. Please select one:")
                    availableModels.forEachIndexed { index, s -> println("  ${index + 1}: $s") }
                    print("Enter number: ")
                    val choice = readlnOrNull()?.toIntOrNull()
                    if (choice != null && choice in 1..availableModels.size) {
                        model = availableModels[choice - 1]
                    } else {
                        println("Invalid selection. Aborting.")
                        return
                    }
                }
            }
        }

        val outputFile = output?.let { File(it) } ?: File(inputFile.parent, "${inputFile.nameWithoutExtension}.jsonl")

        runBlocking {
            processDocument(inputFile, outputFile, model!!)
        }
    }
}

private suspend fun processDocument(inputFile: File, outputFile: File, model: String) {
    println("Extracting text from ${inputFile.name}...")
    val text = DocumentProcessor.extractText(inputFile)
    println("Generating JSONL with Ollama model: $model...")
    val generatedJsonl = OllamaApi.generateJsonl(text, model)
    outputFile.writeText(generatedJsonl)
    println("Successfully saved JSONL to ${outputFile.absolutePath}")
}

@Composable
@Preview
fun App() {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var jsonlOutput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    val availableModels by remember { mutableStateOf(OllamaModelScanner.getAvailableModels()) }
    var selectedModel by remember { mutableStateOf(availableModels.firstOrNull() ?: "") }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("SFT Pro Doc Extractor", style = MaterialTheme.typography.h5)

                Spacer(modifier = Modifier.height(10.dp))

                if (availableModels.isEmpty()) {
                    Text("Warning: No Ollama models found. Please install a model first.", color = Color.Red)
                }

                // File Selection
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        val fileChooser = JFileChooser()
                        fileChooser.dialogTitle = "Select a Document"
                        fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("PDF & Word Documents", "pdf", "docx")
                        val result = fileChooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val file = fileChooser.selectedFile
                            if (file != null) {
                                selectedFile = file
                                coroutineScope.launch {
                                    status = "Extracting text from ${file.name}..."
                                    try {
                                        val text = DocumentProcessor.extractText(file)
                                        status = "Generating JSONL with Ollama model: $selectedModel..."
                                        jsonlOutput = OllamaApi.generateJsonl(text, selectedModel)
                                        status = "Done"
                                    } catch (e: Exception) {
                                        status = "Error: ${e.message}"
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Browse for Document")
                    }
                    Text(selectedFile?.name ?: "No file selected")
                }

                // Model Selection Dropdown
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ollama Model:")
                    Box {
                        Button(onClick = { modelDropdownExpanded = true }, enabled = availableModels.isNotEmpty()) {
                            Text(selectedModel.ifEmpty { "No models found" })
                        }
                        DropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(onClick = {
                                    selectedModel = model
                                    modelDropdownExpanded = false
                                }) {
                                    Text(model)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Indicator
                Text("Status: $status", style = MaterialTheme.typography.subtitle1)

                // JSONL Output Area
                Text("Generated JSONL Output:", style = MaterialTheme.typography.subtitle2)
                TextField(
                    value = jsonlOutput,
                    onValueChange = { jsonlOutput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Color.Gray),
                    readOnly = false
                )

                // Save Button
                Button(onClick = {
                    if (jsonlOutput.isNotBlank()) {
                        val fileChooser = JFileChooser()
                        fileChooser.dialogTitle = "Save JSONL file"
                        val result = fileChooser.showSaveDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            var fileToSave = fileChooser.selectedFile
                            fileToSave?.let {
                                // Ensure it has a .jsonl extension
                                val finalFile = if (!it.name.endsWith(".jsonl")) {
                                    File(it.absolutePath + ".jsonl")
                                } else {
                                    it
                                }
                                finalFile.writeText(jsonlOutput)
                                status = "Saved to ${finalFile.name}"
                            }
                        }
                    }
                }, enabled = jsonlOutput.isNotBlank()) {
                    Text("Save JSONL")
                }
            }
        }
    }
}