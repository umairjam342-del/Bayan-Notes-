package com.example.bayan.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.bayan.data.model.Note
import com.example.bayan.data.quran.QuranData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteExporter {

    private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault())

    /**
     * Converts a Note into a beautifully structured Markdown document.
     */
    fun generateMarkdown(note: Note, surahName: String? = null): String {
        val sName = surahName ?: QuranData.surahs.find { it.number == note.surahNumber.toInt() }?.nameTransliterated
        val surahRef = if (note.surahNumber > 0) {
            val ayahRef = if (note.ayahStart > 0) {
                if (note.ayahEnd > note.ayahStart) "${note.ayahStart}–${note.ayahEnd}"
                else "${note.ayahStart}"
            } else "Full Surah"
            "Surah ${sName ?: note.surahNumber} (${note.surahNumber}:$ayahRef)"
        } else "General Study"

        val categoryFormatted = note.category.replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        val tagsFormatted = if (note.tags.isNotEmpty()) {
            note.tags.joinToString(" ") { "#$it" }
        } else "None"

        val dateStr = note.createdAt?.toDate()?.let { DATE_FORMAT.format(it) } ?: DATE_FORMAT.format(Date())

        return buildString {
            appendLine("# ${note.title}")
            appendLine()
            appendLine("**Category:** $categoryFormatted  ")
            appendLine("**Quranic Reference:** $surahRef  ")
            appendLine("**Tags:** $tagsFormatted  ")
            appendLine("**Date:** $dateStr  ")
            appendLine()
            appendLine("---")
            appendLine()

            if (note.arabicSnippet.isNotBlank()) {
                appendLine("### Qur'anic Text (Arabic)")
                appendLine()
                appendLine("> **${note.arabicSnippet}**")
                appendLine()
            }

            if (note.translationSnippet.isNotBlank()) {
                appendLine("### Translation Reference")
                appendLine()
                appendLine("> *\"${note.translationSnippet}\"*")
                appendLine()
            }

            if (note.arabicSnippet.isNotBlank() || note.translationSnippet.isNotBlank()) {
                appendLine("---")
                appendLine()
            }

            appendLine("### Scholarly Reflections & Commentary")
            appendLine()
            appendLine(note.content)
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("*Exported from **Bayan Notes** — Quran Study Studio & Theological Research*")
        }
    }

    /**
     * Converts a collection of notes into a unified study compendium in Markdown.
     */
    fun generateCompendiumMarkdown(notes: List<Note>, title: String = "Bayan Quran Study Compendium"): String {
        val dateStr = DATE_FORMAT.format(Date())
        return buildString {
            appendLine("# $title")
            appendLine()
            appendLine("*Generated on $dateStr • Total Notes: ${notes.size}*")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Table of Contents")
            appendLine()
            notes.forEachIndexed { index, note ->
                val sName = QuranData.surahs.find { it.number == note.surahNumber.toInt() }?.nameTransliterated
                val ref = if (note.surahNumber > 0) " (${sName ?: "Surah ${note.surahNumber}"})" else ""
                appendLine("${index + 1}. [${note.title}](#note-${index + 1})$ref")
            }
            appendLine()
            appendLine("---")
            appendLine()

            notes.forEachIndexed { index, note ->
                appendLine("<a name=\"note-${index + 1}\"></a>")
                appendLine(generateMarkdown(note))
                appendLine()
                appendLine("---")
                appendLine()
            }
        }
    }

    /**
     * Generates a styled, high-resolution PDF document for a single note.
     */
    fun generatePdf(context: Context, note: Note, surahName: String? = null): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitizedTitle = note.title.ifBlank { "Study_Note" }
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
            .take(30)
        val file = File(exportDir, "Bayan_Note_${sanitizedTitle}_${TIME_FORMAT.format(Date())}.pdf")

        val sName = surahName ?: QuranData.surahs.find { it.number == note.surahNumber.toInt() }?.nameTransliterated
        val surahRef = if (note.surahNumber > 0) {
            val ayahRef = if (note.ayahStart > 0) {
                if (note.ayahEnd > note.ayahStart) "${note.ayahStart}–${note.ayahEnd}"
                else "${note.ayahStart}"
            } else "Full Surah"
            "Surah ${sName ?: note.surahNumber} (${note.surahNumber}:$ayahRef)"
        } else "General Study"

        val categoryFormatted = note.category.replace("_", " ").uppercase()
        val dateStr = note.createdAt?.toDate()?.let { DATE_FORMAT.format(it) } ?: DATE_FORMAT.format(Date())

        val pdfDocument = PdfDocument()

        // Page setup: Standard A4 (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val contentWidth = (pageWidth - margin * 2).toInt()

        // Paints
        val emeraldHeaderPaint = Paint().apply {
            color = Color.parseColor("#0C4033")
            style = Paint.Style.FILL
        }
        val goldLinePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val headerTitlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val noteTitlePaint = TextPaint().apply {
            color = Color.parseColor("#0C4033")
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val categoryBgPaint = Paint().apply {
            color = Color.parseColor("#E6F2EE")
            style = Paint.Style.FILL
        }
        val categoryTextPaint = TextPaint().apply {
            color = Color.parseColor("#0C4033")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val metaTextPaint = TextPaint().apply {
            color = Color.parseColor("#555555")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sectionHeadingPaint = TextPaint().apply {
            color = Color.parseColor("#0C4033")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val arabicQuoteBoxPaint = Paint().apply {
            color = Color.parseColor("#F6FAF8")
            style = Paint.Style.FILL
        }
        val arabicQuoteBorderPaint = Paint().apply {
            color = Color.parseColor("#C4DFD7")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val arabicTextPaint = TextPaint().apply {
            color = Color.parseColor("#083329")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val translationTextPaint = TextPaint().apply {
            color = Color.parseColor("#33443C")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        val bodyTextPaint = TextPaint().apply {
            color = Color.parseColor("#1C2522")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val tagBgPaint = Paint().apply {
            color = Color.parseColor("#EEEEEE")
            style = Paint.Style.FILL
        }
        val tagTextPaint = TextPaint().apply {
            color = Color.parseColor("#666666")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#888888")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
            // Top Header Banner
            c.drawRect(0f, 0f, pageWidth.toFloat(), 48f, emeraldHeaderPaint)
            c.drawLine(0f, 48f, pageWidth.toFloat(), 48f, goldLinePaint)
            c.drawText("BAYAN NOTES • QURAN STUDY & TAFSIR STUDIO", margin, 30f, headerTitlePaint)

            // Footer
            val footerText = "Generated with Bayan Notes Studio • $dateStr • Page $pNum"
            val footerWidth = footerPaint.measureText(footerText)
            c.drawText(footerText, (pageWidth - footerWidth) / 2f, pageHeight - 20f, footerPaint)
            c.drawLine(margin, pageHeight - 32f, pageWidth - margin, pageHeight - 32f, Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 0.8f
            })
        }

        drawHeaderAndFooter(canvas, pageNumber)

        var currentY = 74f

        // Note Title
        val titleLayout = createStaticLayout(note.title, noteTitlePaint, contentWidth)
        canvas.save()
        canvas.translate(margin, currentY)
        titleLayout.draw(canvas)
        canvas.restore()
        currentY += titleLayout.height + 12f

        // Category & Surah Pill row
        val catWidth = categoryTextPaint.measureText(categoryFormatted) + 16f
        val catRect = RectF(margin, currentY, margin + catWidth, currentY + 18f)
        canvas.drawRoundRect(catRect, 6f, 6f, categoryBgPaint)
        canvas.drawText(categoryFormatted, margin + 8f, currentY + 13f, categoryTextPaint)

        val metaString = " |  $surahRef  |  $dateStr"
        canvas.drawText(metaString, margin + catWidth + 8f, currentY + 13f, metaTextPaint)
        currentY += 28f

        // Thin separator
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, Paint().apply {
            color = Color.parseColor("#EAEAEA")
            strokeWidth = 1f
        })
        currentY += 16f

        // Arabic Verse Box (if present)
        if (note.arabicSnippet.isNotBlank()) {
            canvas.drawText("QUR'ANIC TEXT", margin, currentY, sectionHeadingPaint)
            currentY += 12f

            val arabicLayout = createStaticLayout(note.arabicSnippet, arabicTextPaint, contentWidth - 24)
            val boxHeight = arabicLayout.height + 24f
            val quoteBoxRect = RectF(margin, currentY, pageWidth - margin, currentY + boxHeight)
            canvas.drawRoundRect(quoteBoxRect, 8f, 8f, arabicQuoteBoxPaint)
            canvas.drawRoundRect(quoteBoxRect, 8f, 8f, arabicQuoteBorderPaint)

            canvas.save()
            canvas.translate(margin + 12f, currentY + 12f)
            arabicLayout.draw(canvas)
            canvas.restore()

            currentY += boxHeight + 14f
        }

        // Translation snippet (if present)
        if (note.translationSnippet.isNotBlank()) {
            canvas.drawText("TRANSLATION REFERENCE", margin, currentY, sectionHeadingPaint)
            currentY += 12f

            val transLayout = createStaticLayout("\"${note.translationSnippet}\"", translationTextPaint, contentWidth)
            canvas.save()
            canvas.translate(margin, currentY)
            transLayout.draw(canvas)
            canvas.restore()

            currentY += transLayout.height + 16f
        }

        // Scholarly Commentary & Reflections
        canvas.drawText("SCHOLARLY REFLECTIONS & COMMENTARY", margin, currentY, sectionHeadingPaint)
        currentY += 14f

        val bodyLayout = createStaticLayout(note.content, bodyTextPaint, contentWidth)

        // Check if content fits on page 1 or needs multiple pages
        val availableOnPage1 = (pageHeight - 60f) - currentY
        if (bodyLayout.height <= availableOnPage1) {
            canvas.save()
            canvas.translate(margin, currentY)
            bodyLayout.draw(canvas)
            canvas.restore()
            currentY += bodyLayout.height + 16f

            // Tags
            if (note.tags.isNotEmpty()) {
                var tagX = margin
                note.tags.forEach { tag ->
                    val tagStr = "#$tag"
                    val tagW = tagTextPaint.measureText(tagStr) + 12f
                    if (tagX + tagW < pageWidth - margin) {
                        canvas.drawRoundRect(RectF(tagX, currentY, tagX + tagW, currentY + 16f), 4f, 4f, tagBgPaint)
                        canvas.drawText(tagStr, tagX + 6f, currentY + 12f, tagTextPaint)
                        tagX += tagW + 6f
                    }
                }
            }

            pdfDocument.finishPage(page)
        } else {
            // Multi-page handling: split body by line
            var lineIndex = 0
            val lineCount = bodyLayout.lineCount

            // First page partial drawing
            pdfDocument.finishPage(page)

            // Start new page for the full body content cleanly
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawHeaderAndFooter(canvas, pageNumber)
            currentY = 68f

            canvas.drawText("SCHOLARLY REFLECTIONS (Continued)", margin, currentY, sectionHeadingPaint)
            currentY += 14f

            canvas.save()
            canvas.translate(margin, currentY)
            bodyLayout.draw(canvas)
            canvas.restore()

            currentY += bodyLayout.height + 16f

            // Tags
            if (note.tags.isNotEmpty()) {
                var tagX = margin
                note.tags.forEach { tag ->
                    val tagStr = "#$tag"
                    val tagW = tagTextPaint.measureText(tagStr) + 12f
                    if (tagX + tagW < pageWidth - margin) {
                        canvas.drawRoundRect(RectF(tagX, currentY, tagX + tagW, currentY + 16f), 4f, 4f, tagBgPaint)
                        canvas.drawText(tagStr, tagX + 6f, currentY + 12f, tagTextPaint)
                        tagX += tagW + 6f
                    }
                }
            }

            pdfDocument.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    /**
     * Generates a comprehensive multi-note study compendium in PDF format.
     */
    fun generatePdfCompendium(context: Context, notes: List<Note>, title: String = "Bayan Quran Study Compendium"): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Bayan_Study_Compendium_${TIME_FORMAT.format(Date())}.pdf")

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val contentWidth = (pageWidth - margin * 2).toInt()
        val dateStr = DATE_FORMAT.format(Date())

        val emeraldHeaderPaint = Paint().apply {
            color = Color.parseColor("#0C4033")
            style = Paint.Style.FILL
        }
        val goldLinePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val headerTitlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val noteTitlePaint = TextPaint().apply {
            color = Color.parseColor("#0C4033")
            textSize = 16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyTextPaint = TextPaint().apply {
            color = Color.parseColor("#1C2522")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#888888")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Cover / Summary Page
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Cover Design
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 180f, emeraldHeaderPaint)
        canvas.drawLine(0f, 180f, pageWidth.toFloat(), 180f, goldLinePaint)

        val coverTitlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val coverSubPaint = TextPaint().apply {
            color = Color.parseColor("#E0F2EC")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText("BAYAN NOTES", margin, 75f, coverSubPaint)
        canvas.drawText("Quran Study & Research Compendium", margin, 110f, coverTitlePaint)
        canvas.drawText("Collection of ${notes.size} Research Notes • $dateStr", margin, 140f, coverSubPaint)

        var yPos = 220f
        val tocHeadingPaint = TextPaint().apply {
            color = Color.parseColor("#0C4033")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TABLE OF CONTENTS", margin, yPos, tocHeadingPaint)
        yPos += 20f

        val tocItemPaint = TextPaint().apply {
            color = Color.parseColor("#333333")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        notes.take(24).forEachIndexed { idx, n ->
            val sName = QuranData.surahs.find { it.number == n.surahNumber.toInt() }?.nameTransliterated
            val sRef = if (n.surahNumber > 0) " (${sName ?: "Surah ${n.surahNumber}"})" else ""
            val tocEntry = "${idx + 1}. ${n.title.take(50)}$sRef"
            canvas.drawText(tocEntry, margin + 8f, yPos, tocItemPaint)
            yPos += 18f
        }

        if (notes.size > 24) {
            canvas.drawText("... and ${notes.size - 24} additional notes", margin + 8f, yPos, tocItemPaint)
        }

        // Footer on cover
        val coverFooter = "Generated with Bayan Notes Studio • Page 1"
        canvas.drawText(coverFooter, (pageWidth - footerPaint.measureText(coverFooter)) / 2f, pageHeight - 20f, footerPaint)
        pdfDocument.finishPage(page)

        // Now render each note on subsequent pages
        notes.forEach { note ->
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            // Top Header
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 40f, emeraldHeaderPaint)
            canvas.drawLine(0f, 40f, pageWidth.toFloat(), 40f, goldLinePaint)
            canvas.drawText("BAYAN NOTES • STUDY COMPENDIUM", margin, 25f, headerTitlePaint)

            var noteY = 64f

            // Title
            val titleL = createStaticLayout(note.title, noteTitlePaint, contentWidth)
            canvas.save()
            canvas.translate(margin, noteY)
            titleL.draw(canvas)
            canvas.restore()
            noteY += titleL.height + 10f

            // Surah & Category
            val sName = QuranData.surahs.find { it.number == note.surahNumber.toInt() }?.nameTransliterated
            val meta = "Category: ${note.category.uppercase()} | Ref: ${if (note.surahNumber > 0) "Surah ${sName ?: note.surahNumber}" else "General"}"
            canvas.drawText(meta, margin, noteY, TextPaint().apply {
                color = Color.parseColor("#555555")
                textSize = 9.5f
                isAntiAlias = true
            })
            noteY += 18f

            // Arabic Snippet if any
            if (note.arabicSnippet.isNotBlank()) {
                val arL = createStaticLayout(note.arabicSnippet, TextPaint().apply {
                    color = Color.parseColor("#0C4033")
                    textSize = 12.5f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }, contentWidth - 16)
                val boxH = arL.height + 16f
                canvas.drawRoundRect(RectF(margin, noteY, pageWidth - margin, noteY + boxH), 6f, 6f, Paint().apply {
                    color = Color.parseColor("#F4F8F6")
                    style = Paint.Style.FILL
                })
                canvas.save()
                canvas.translate(margin + 8f, noteY + 8f)
                arL.draw(canvas)
                canvas.restore()
                noteY += boxH + 12f
            }

            // Body
            val maxAvailable = pageHeight - 50f - noteY
            val bodyL = createStaticLayout(note.content, bodyTextPaint, contentWidth)
            canvas.save()
            canvas.translate(margin, noteY)
            bodyL.draw(canvas)
            canvas.restore()

            // Footer
            val noteFooter = "Page $pageNum • Bayan Notes Studio"
            canvas.drawText(noteFooter, (pageWidth - footerPaint.measureText(noteFooter)) / 2f, pageHeight - 20f, footerPaint)
            canvas.drawLine(margin, pageHeight - 30f, pageWidth - margin, pageHeight - 30f, Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 0.8f
            })

            pdfDocument.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    /**
     * Creates a text file with .md extension in cache/exports.
     */
    fun saveMarkdownFile(context: Context, content: String, baseName: String): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitized = baseName.ifBlank { "Study_Note" }
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
            .take(30)
        val file = File(exportDir, "${sanitized}_${TIME_FORMAT.format(Date())}.md")
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    /**
     * Shares a file via Android Intent.ACTION_SEND chooser with FileProvider URI.
     */
    fun shareFile(context: Context, file: File, mimeType: String, subject: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share $subject via...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Copies Markdown text directly to the system clipboard.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun createStaticLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(10))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.25f)
            .setIncludePad(true)
            .build()
    }
}
