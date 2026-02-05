import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
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
import kotlinx.cli.default

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
        val model by parser.option(ArgType.String, shortName = "m", fullName = "model", description = "Ollama model to use").default("llama3:latest")
        parser.parse(args)

        val inputFile = input?.let { File(it) }
        if (inputFile == null || !inputFile.exists()) {
            println("Error: Input file not specified or does not exist.")
            return
        }

        val outputFile = output?.let { File(it) } ?: File(inputFile.parent, "${inputFile.nameWithoutExtension}.jsonl")

        runBlocking {
            processDocument(inputFile, outputFile, model)
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
    var extractedText by remember { mutableStateOf("") }
    var jsonlOutput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    val model = "llama3:latest" // Or make this configurable in the UI
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

                // File Selection
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        val fileChooser = JFileChooser()
                        fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("PDF & Word Documents", "pdf", "docx")
                        val result = fileChooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val file = fileChooser.selectedFile
                            if (file != null) {
                                selectedFile = file
                                coroutineScope.launch {
                                    status = "Extracting text from ${file.name}..."
                                    val text = DocumentProcessor.extractText(file)
                                    extractedText = text // Keep for potential display/debugging

                                    status = "Generating JSONL with Ollama model: $model..."
                                    val generatedJsonl = OllamaApi.generateJsonl(text, model)
                                    jsonlOutput = generatedJsonl
                                    status = "Done"
                                }
                            }
                        }
                    }) {
                        Text("Browse for Document")
                    }
                    Text(selectedFile?.name ?: "No file selected")
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
                    readOnly = false // Allow editing for now
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