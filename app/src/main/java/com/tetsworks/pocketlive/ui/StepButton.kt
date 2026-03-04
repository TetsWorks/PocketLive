package com.tetsworks.pocketlive.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class StepButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var isActive: Boolean = false
        set(value) { field = value; invalidate() }

    var isCurrent: Boolean = false
        set(value) { field = value; invalidate() }

    var trackColor: Int = 0xFF1E88E5.toInt()
        set(value) { field = value; invalidate() }

    private val paintActive = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintInactive = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintCurrent = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    init {
        paintInactive.color = 0xFF2A2A2A.toInt()
        paintCurrent.color  = 0x44FFFFFF.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = height * 0.18f
        rect.set(2f, 2f, width - 2f, height - 2f)

        paintActive.color = trackColor

        // Fundo
        canvas.drawRoundRect(rect, radius, radius,
            if (isActive) paintActive else paintInactive)

        // Highlight do step atual
        if (isCurrent) {
            canvas.drawRoundRect(rect, radius, radius, paintCurrent)
        }
    }
}
