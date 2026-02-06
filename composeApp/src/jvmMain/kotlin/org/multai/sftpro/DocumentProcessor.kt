package org.multai.sftpro

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

object DocumentProcessor {

    private const val TEXT_LIMIT = 16000

    fun extractText(file: File): String {
        val text = when (file.extension.lowercase()) {
            "pdf" -> extractTextFromPdf(file)
            "docx" -> extractTextFromDocx(file)
            else -> "Unsupported file type: ${file.extension}"
        }
        return text.take(TEXT_LIMIT)  // Cleaner than substring
    }

    private fun extractTextFromPdf(file: File): String {
        return try {
            PDDocument.load(file).use { document ->
                PDFTextStripper().getText(document)
            }
        } catch (e: Exception) {
            "Error extracting text from PDF: ${e.message}"
        }
    }

    private fun extractTextFromDocx(file: File): String {
        return try {
            FileInputStream(file).use { fis ->
                XWPFDocument(fis).use { document ->
                    XWPFWordExtractor(document).text
                }
            }
        } catch (e: Exception) {
            "Error extracting text from DOCX: ${e.message}"
        }
    }
}