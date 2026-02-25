package com.varuna.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.google.firebase.firestore.DocumentSnapshot
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Generates a PDF report containing:
 * - Village Name + Date
 * - Water Parameters Table
 * - WQI Score + Classification
 * - Disease Risk Output
 * - WHO + BIS guideline comparison
 * - Purification Recommendations
 * - Emergency Instructions
 */
object PdfReportGenerator {

    fun generateReport(
        context: Context,
        wqDoc: DocumentSnapshot?,
        drDoc: DocumentSnapshot?,
        uid: String
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595  // A4 width in points
            val pageHeight = 842 // A4 height in points

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = 40f
            val leftMargin = 40f

            // Paints
            val titlePaint = Paint().apply {
                color = 0xFF1565C0.toInt()
                textSize = 24f
                isFakeBoldText = true
            }
            val headingPaint = Paint().apply {
                color = 0xFF0D47A1.toInt()
                textSize = 16f
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val smallPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9f
            }
            val linePaint = Paint().apply {
                color = 0xFFBBDEFB.toInt()
                strokeWidth = 1f
            }
            val safePaint = Paint().apply { color = 0xFF2E7D32.toInt(); textSize = 14f; isFakeBoldText = true }
            val moderatePaint = Paint().apply { color = 0xFFF57F17.toInt(); textSize = 14f; isFakeBoldText = true }
            val unsafePaint = Paint().apply { color = 0xFFB71C1C.toInt(); textSize = 14f; isFakeBoldText = true }

            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())

            // ===== HEADER =====
            canvas.drawText("VARUNA APP", leftMargin, y, titlePaint)
            y += 8f
            canvas.drawText("Water Quality & Disease Risk Assessment Report", leftMargin, y + 18f, headingPaint)
            y += 32f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            // ===== REPORT META =====
            val villageName = wqDoc?.getString("villageName") ?: "N/A"
            val timestamp = wqDoc?.getLong("timestamp") ?: System.currentTimeMillis()
            canvas.drawText("📍 Village / Location: $villageName", leftMargin, y, bodyPaint)
            y += 18f
            canvas.drawText("📅 Report Date: ${sdf.format(timestamp)}", leftMargin, y, bodyPaint)
            y += 18f
            canvas.drawText("🆔 User ID: $uid", leftMargin, y, smallPaint)
            y += 24f

            // ===== WATER PARAMETERS TABLE =====
            canvas.drawText("WATER QUALITY PARAMETERS", leftMargin, y, headingPaint)
            y += 6f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            val params = listOf(
                "pH" to (wqDoc?.getDouble("ph")?.let { String.format("%.2f", it) } ?: "N/A"),
                "TDS (mg/L)" to (wqDoc?.getDouble("tds")?.let { String.format("%.1f", it) } ?: "N/A"),
                "Turbidity (NTU)" to (wqDoc?.getDouble("turbidity")?.let { String.format("%.1f", it) } ?: "N/A"),
                "Hardness (mg/L)" to (wqDoc?.getDouble("hardness")?.let { String.format("%.1f", it) } ?: "N/A"),
                "Temperature (°C)" to (wqDoc?.getDouble("temperature")?.let { String.format("%.1f", it) } ?: "N/A"),
                "Chloride (mg/L)" to (wqDoc?.getDouble("chloride")?.let { String.format("%.1f", it) } ?: "N/A"),
                "Dissolved Oxygen (mg/L)" to (wqDoc?.getDouble("dissolvedOxygen")?.let { String.format("%.1f", it) } ?: "N/A")
            )

            val whoLimits = mapOf(
                "pH" to "6.5–8.5",
                "TDS (mg/L)" to "≤500",
                "Turbidity (NTU)" to "≤4",
                "Hardness (mg/L)" to "≤300",
                "Temperature (°C)" to "10–30",
                "Chloride (mg/L)" to "≤250",
                "Dissolved Oxygen (mg/L)" to "≥5"
            )

            // Table header
            canvas.drawText("Parameter", leftMargin, y, bodyPaint.apply { isFakeBoldText = true })
            canvas.drawText("Measured Value", 240f, y, bodyPaint)
            canvas.drawText("WHO/BIS Limit", 380f, y, bodyPaint)
            y += 4f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 14f
            bodyPaint.isFakeBoldText = false

            for ((param, value) in params) {
                canvas.drawText(param, leftMargin, y, bodyPaint)
                canvas.drawText(value, 240f, y, bodyPaint)
                canvas.drawText(whoLimits[param] ?: "N/A", 380f, y, bodyPaint)
                y += 15f
            }

            y += 8f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            // ===== WQI SCORE =====
            val wqi = wqDoc?.getDouble("wqiScore") ?: 0.0
            val classification = wqDoc?.getString("classification") ?: "Unknown"
            canvas.drawText("WATER QUALITY INDEX (WQI)", leftMargin, y, headingPaint)
            y += 18f
            canvas.drawText("WQI Score:", leftMargin, y, bodyPaint)
            val wqiPaint = when (classification) {
                "Safe" -> safePaint
                "Moderate" -> moderatePaint
                else -> unsafePaint
            }
            canvas.drawText(String.format("%.1f / 100", wqi), 200f, y, wqiPaint)
            y += 18f
            canvas.drawText("Classification:", leftMargin, y, bodyPaint)
            canvas.drawText("● $classification", 200f, y, wqiPaint)
            y += 20f

            // WHO Issues
            val whoIssues = wqDoc?.get("whoComplianceIssues") as? List<*>
            if (!whoIssues.isNullOrEmpty()) {
                canvas.drawText("⚠️ WHO/BIS Violations Detected:", leftMargin, y, unsafePaint.apply { textSize = 11f })
                y += 14f
                whoIssues.forEach { issue ->
                    canvas.drawText("  • $issue", leftMargin, y, bodyPaint)
                    y += 13f
                }
            }
            y += 8f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            // ===== DISEASE RISK =====
            canvas.drawText("DISEASE RISK PREDICTION", leftMargin, y, headingPaint)
            y += 18f
            val choleraRisk = drDoc?.getString("choleraRisk") ?: "N/A"
            val typhoidRisk = drDoc?.getString("typhoidRisk") ?: "N/A"
            val diarrheaRisk = drDoc?.getString("diarrheaRisk") ?: "N/A"
            canvas.drawText("Cholera Risk: $choleraRisk", leftMargin, y, bodyPaint)
            y += 15f
            canvas.drawText("Typhoid Risk: $typhoidRisk", leftMargin, y, bodyPaint)
            y += 15f
            canvas.drawText("Diarrhea Risk: $diarrheaRisk", leftMargin, y, bodyPaint)
            y += 20f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            // ===== PURIFICATION SUGGESTIONS =====
            canvas.drawText("PURIFICATION RECOMMENDATIONS", leftMargin, y, headingPaint)
            y += 16f
            val suggestions = wqDoc?.get("purificationSuggestions") as? List<*>
            suggestions?.forEach { suggestion ->
                val lines = wrapText(suggestion.toString(), 70)
                lines.forEach { line ->
                    canvas.drawText(line, leftMargin, y, bodyPaint)
                    y += 13f
                }
            }
            y += 8f
            canvas.drawLine(leftMargin, y, (pageWidth - leftMargin), y, linePaint)
            y += 16f

            // ===== EMERGENCY GUIDELINES =====
            if (classification != "Safe") {
                canvas.drawText("EMERGENCY INSTRUCTIONS", leftMargin, y, unsafePaint.apply { textSize = 14f })
                y += 16f
                val guidelines = wqDoc?.get("emergencyGuidelines") as? List<*>
                guidelines?.forEach { guideline ->
                    canvas.drawText(guideline.toString(), leftMargin, y, bodyPaint)
                    y += 14f
                }
                y += 8f
            }

            // ===== FOOTER =====
            val footerPaint = Paint().apply { color = Color.GRAY; textSize = 9f }
            canvas.drawLine(leftMargin, pageHeight - 40f, (pageWidth - leftMargin), pageHeight - 40f, linePaint)
            canvas.drawText(
                "Generated by Varuna App | ${sdf.format(System.currentTimeMillis())} | For official use only",
                leftMargin, pageHeight - 25f, footerPaint
            )

            pdfDocument.finishPage(page)

            // Save PDF
            val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
            val fileName = "Varuna_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.pdf"
            val file = File(outputDir, fileName)
            FileOutputStream(file).use { fos -> pdfDocument.writeTo(fos) }
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun wrapText(text: String, maxChars: Int): List<String> {
        if (text.length <= maxChars) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            if ((currentLine + " " + word).trim().length <= maxChars) {
                currentLine = (currentLine + " " + word).trim()
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
