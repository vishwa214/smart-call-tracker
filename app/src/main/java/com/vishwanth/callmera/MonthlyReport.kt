package com.vishwanth.callmera

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.vishwanth.callmera.database.CallEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
object MonthlyReport {
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {

        val output =
            Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect =
            Rect(0, 0, bitmap.width, bitmap.height)

        canvas.drawARGB(0, 0, 0, 0)

        canvas.drawCircle(
            bitmap.width / 2f,
            bitmap.height / 2f,
            bitmap.width / 2f,
            paint
        )

        paint.xfermode =
            PorterDuffXfermode(
                PorterDuff.Mode.SRC_IN
            )

        canvas.drawBitmap(
            bitmap,
            rect,
            rect,
            paint
        )

        return output
    }
    fun generatePdf(
        context: Context,
        month: String,
        calls: List<CallEntity>
    ): File {

        val countedDays =
            calls.count {
                it.countedDay
            }

        val totalAmount =
            calls.sumOf {
                it.amount
            }

        val pdfDocument =
            PdfDocument()

        val pageInfo =
            PdfDocument.PageInfo.Builder(
                1200,
                2000,
                1
            ).create()

        val page =
            pdfDocument.startPage(pageInfo)

        val canvas =
            page.canvas
        val backgroundPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#120B3A")
        }

        canvas.drawRect(
            0f,
            0f,
            pageInfo.pageWidth.toFloat(),
            pageInfo.pageHeight.toFloat(),
            backgroundPaint
        )

        val titlePaint = Paint().apply {
            textSize = 32f
            color = android.graphics.Color.WHITE
            isFakeBoldText = true
        }


        val subPaint = Paint().apply {
            textSize = 24f
            color = android.graphics.Color.parseColor("#BB86FC")
            isFakeBoldText = true
        }

        val normalPaint = Paint().apply {
            textSize = 20f
            color = android.graphics.Color.WHITE
        }
        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#7B3FF2")
        }
        val successPaint = Paint().apply {
            textSize = 20f
            color = android.graphics.Color.parseColor("#00E676")
        }
        val bitmap =
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.app_icon
            )

        val scaledLogo =
            Bitmap.createScaledBitmap(
                bitmap,
                100,
                100,
                false
            )

        val circularLogo =
            getCircularBitmap(
                scaledLogo
            )
        canvas.drawRect(
            0f,
            0f,
            pageInfo.pageWidth.toFloat(),
            140f,
            headerPaint
        )
        canvas.drawBitmap(
            circularLogo,
            30f,
            20f,
            null
        )
        canvas.drawText(
            "SMART CALL TRACKER",
            150f,
            70f,
            titlePaint
        )

        canvas.drawText(
            "Monthly Performance Report",
            150f,
            110f,
            subPaint
        )



        canvas.drawText(
            "Month : $month",
            50f,
            190f,
            normalPaint
        )

        canvas.drawText(
            "--------------------------------------",
            50f,
            230f,
            normalPaint
        )

        canvas.drawText(
            "SUMMARY",
            50f,
            280f,
            subPaint
        )

        canvas.drawText(
            "Total Counted Days : $countedDays",
            50f,
            340f,
            normalPaint
        )

        canvas.drawText(
            "Total Amount : ₹$totalAmount",
            50f,
            390f,
            normalPaint
        )

        canvas.drawText(
            "--------------------------------------",
            50f,
            440f,
            normalPaint
        )

        canvas.drawText(
            "COUNTED DAY DETAILS",
            50f,
            500f,
            subPaint
        )


        var y = 560f

        calls
            .filter { it.countedDay }
            .forEach { call ->

                canvas.drawText(
                    "Date : ${call.callDate}",
                    50f,
                    y,
                    normalPaint
                )

                y += 40f



                y += 40f

                canvas.drawText(
                    "Amount : ₹${call.amount}",
                    80f,
                    y,
                    successPaint
                )

                y += 50f

                canvas.drawText(
                    "--------------------------------------",
                    50f,
                    y,
                    normalPaint
                )

                y += 50f
            }

        val currentTime =
            SimpleDateFormat(
                "dd-MM-yyyy hh:mm a",
                Locale.getDefault()
            ).format(Date())

        canvas.drawText(
            "Generated On : $currentTime",
            50f,
            y + 50f,
            normalPaint
        )

        pdfDocument.finishPage(page)

        val safeMonth =
            month.replace(" ", "_")

        val file =
            File(
                context.cacheDir,
                "CallMeRa_Report_$safeMonth.pdf"
            )

        pdfDocument.writeTo(
            FileOutputStream(file)
        )

        pdfDocument.close()

        return file
    }
}