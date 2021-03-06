package com.atanana

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.File
import java.io.InputStream

class DocxProcessor(file: File) : DocumentProcessor<XWPFDocument>(file) {
    override fun createDocument(inputStream: InputStream): XWPFDocument {
        return XWPFDocument(inputStream)
    }

    override fun hideInfo(document: XWPFDocument) {
        var waitNextQuestion = false
        var i = 0

        while (i < document.paragraphs.size) {
            val paragraph = document.paragraphs[i]
            val text = paragraph.text

            if (text.startsWith("ответ:", true)) {
                waitNextQuestion = true
                hideEndText(paragraph)
            } else if (text.matches(QUESTION_REGEX) || text.matches(TOUR_REGEX)) {
                waitNextQuestion = false
            } else if (waitNextQuestion) {
                if (shouldBePartialHidden(text)) {
                    hideEndText(paragraph)
                } else {
                    hideWholeText(paragraph)
                }
            }

            i++
        }
    }

    private fun hideWholeText(paragraph: XWPFParagraph) {
        val text = paragraph.text
        clearAllRuns(paragraph)
        addWhiteText(paragraph, text)
    }

    private fun hideEndText(paragraph: XWPFParagraph) {
        val text = paragraph.text
        val mark = text.indexOf(':')

        clearAllRuns(paragraph)

        val run = paragraph.createRun()
        run.setText(text.substring(0, mark + 1))
        run.isBold = true

        addWhiteText(paragraph, text.substring(mark + 1))
    }

    private fun addWhiteText(paragraph: XWPFParagraph, text: String) {
        val hiddenRun = paragraph.createRun()
        hiddenRun.setText(text)
        hiddenRun.color = "FFFFFF"
    }

    private fun clearAllRuns(paragraph: XWPFParagraph) {
        for (i in 0..paragraph.runs.size) {
            try {
                paragraph.removeRun(0)
            } catch(e: Exception) {
                println(e)
            }
        }
    }

    override fun writeFile(document: XWPFDocument, file: File) {
        file.outputStream().use { document.write(it) }
    }
}