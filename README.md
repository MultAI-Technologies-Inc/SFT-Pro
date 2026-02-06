# SFT Pro Doc Extractor

SFT Pro Doc Extractor is a desktop tool built with Kotlin Multiplatform that extracts text from documents (`.pdf`, `.docx`) and uses a local Ollama instance to generate structured JSONL output suitable for Supervised Fine-Tuning (SFT) of language models.

The application can be run in two modes:
1.  **GUI Mode**: An intuitive graphical interface for selecting files, choosing models, and viewing/saving results.
2.  **CLI Mode**: A command-line interface for scripting and automation.

## Prerequisites

Before using this tool, you must have Ollama installed and running on your system. You also need at least one model pulled.

You can pull a model using the following command:
```shell
ollama pull llama3
```

## Installation (Windows)

First, build the distributable application package.

```shell
.\gradlew.bat :composeApp:packageExe
```

This command will create a standalone `.exe` installer.

1.  **Run the Installer**: Navigate to `composeApp\build\compose\binaries\main\exe` and run the installer, `sftpro-1.0.0.exe`. Follow the on-screen instructions. By default, it will install to `C:\Users\<YourUser>\AppData\Local\sftpro`.

2.  **Add to Environment Variables**: To use the CLI from any terminal, you need to add the installation directory to your system's `PATH`.
    *   Press `Win + R`, type `sysdm.cpl`, and press Enter.
    *   Go to the `Advanced` tab and click `Environment Variables...`.
    *   Under `System variables` (or `User variables`), find and select the `Path` variable, then click `Edit...`.
    *   Click `New` and add the path to the installation directory. The default is:
        ```
        C:\Users\<YourUser>\AppData\Local\sftpro
        ```
    *   Click `OK` on all windows to save the changes.

3.  **Verify Installation**: Open a new Command Prompt or PowerShell window and run `sftpro.exe --help`. You should see the command-line options.

## Usage

### GUI Mode

To run the graphical interface, simply run the application from your Start Menu or by double-clicking the `sftpro.exe` file in the installation directory.

1.  **Browse for Document**: Click to open a file dialog and select a `.pdf` or `.docx` file.
2.  **Select Model**: Choose an available Ollama model from the dropdown. The app will automatically detect your installed models.
3.  **Generate**: The app will automatically extract text and generate the JSONL output.
4.  **Save**: Click the "Save JSONL" button to save the output to a file.

### CLI Mode

Once you have added the application to your `PATH`, you can use it from any terminal.

**Syntax:**
```shell
sftpro.exe -i <input_file> [-o <output_file>] [-m <model_name>]
```

**Options:**

*   `-i, --input`: (Required) Path to the input document file (`.pdf`, `.docx`).
*   `-o, --output`: (Optional) Path for the output `.jsonl` file. If omitted, it defaults to the same directory and name as the input file, but with a `.jsonl` extension.
*   `-m, --model`: (Optional) The name of the Ollama model to use. If omitted, and you have multiple models installed, you will be prompted to select one.

**Examples:**

*   Process a document and specify the model:
    ```shell
    sftpro.exe -i "C:\Users\bryan\Documents\my_doc.pdf" -m llama3
    ```

*   Process a document and specify the output file:
    ```shell
    sftpro.exe -i "my_doc.docx" -o "training_data.jsonl" -m llama3
    ```

*   Process a document and let the tool prompt for a model selection:
    ```shell
    sftpro.exe -i "my_doc.pdf"
    ```

### Build and Run Desktop (JVM) Application

To run the development version of the desktop app directly from the source code, use the run configuration from your IDE’s toolbar or run it from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…