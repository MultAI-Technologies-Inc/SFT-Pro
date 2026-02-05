# SFT-Pro

SFT-Pro is a native desktop tool designed to simplify the extraction of information from documents (`.pdf`, `.docx`) and format it into JSONL data. This output is perfect for Supervised Fine-Tuning (SFT) of local Large Language Models (LLMs) or for creating datasets for Retrieval-Augmented Generation (RAG).

The application leverages a running Ollama instance to process the extracted text and generate the structured JSONL output.

## Features

- **GUI & CLI:** Use the simple graphical interface for single files or the command-line interface for automation and batch processing.
- **Multiple Document Types:** Supports text extraction from PDF (`.pdf`) and Microsoft Word (`.docx`) files.
- **Local LLM Integration:** Connects to your local Ollama instance to generate high-quality JSONL data.
- **Configurable Model:** Choose which Ollama model to use for data generation via the command line.

## Prerequisites

1.  **Java Development Kit (JDK):** A JDK (version 11 or newer) is required to run the application.
2.  **Ollama:** You must have Ollama installed and running on your machine. You can download it from ollama.com.
3.  **An Ollama Model:** Pull a model to be used for generation. For example:
    ```sh
    ollama pull llama3
    ```

## Usage

SFT-Pro can be run in two modes: GUI mode and CLI mode.

### GUI Mode

To run the graphical interface, simply execute the application JAR file without any command-line arguments.

```sh
# Example of running the JAR
java -jar SFT-Pro.jar
```

1.  Click **"Browse for Document"** to open a file picker.
2.  Select a `.pdf` or `.docx` file.
3.  The application will extract the text and send it to your local Ollama instance.
4.  The generated JSONL will appear in the text area.
5.  You can edit the output if needed.
6.  Click **"Save JSONL"** to save the output to a `.jsonl` file.

### CLI Mode

The Command-Line Interface (CLI) is ideal for scripting and processing multiple files.

**Syntax:**
```sh
java -jar SFT-Pro.jar -i <input_file> [-o <output_file>] [-m <model_name>]
```

**Arguments:**

- `-i`, `--input`: (Required) The path to the input document (`.pdf` or `.docx`).
- `-o`, `--output`: (Optional) The path to save the output `.jsonl` file. If not provided, it defaults to the same directory and name as the input file, but with a `.jsonl` extension.
- `-m`, `--model`: (Optional) The name of the Ollama model to use (e.g., `llama3:latest`, `mistral`). Defaults to `llama3:latest`.

**Example:**
```sh
java -jar SFT-Pro.jar --input "C:\Users\bryan\Documents\my_document.pdf" --output "C:\Users\bryan\Documents\dataset.jsonl" --model "mistral"
```
