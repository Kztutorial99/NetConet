package com.netspeedpro

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val START_ANGLE = 135f
    private val SWEEP_ANGLE = 270f
    private val STROKE_WIDTH_DP = 14f
    private val TICK_COUNT = 9

    private var currentProgress = 0f
    private var animator: ValueAnimator? = null

    var maxSpeed: Float = 200f
    var gaugeColor: Int = 0xFF00D4FF.toInt()
    var label: String = "TAP TO TEST"
    var speedValue: Float = 0f
        private set

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val speedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val unitTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val arcRect = RectF()

    fun setSpeed(speed: Float, animate: Boolean = true) {
        val targetProgress = (speed / maxSpeed).coerceIn(0f, 1f)
        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(currentProgress, targetProgress).apply {
                duration = 400
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    currentProgress = it.animatedValue as Float
                    speedValue = currentProgress * maxSpeed
                    invalidate()
                }
                start()
            }
        } else {
            animator?.cancel()
            currentProgress = targetProgress
            speedValue = speed
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val strokeW = STROKE_WIDTH_DP * density
        val padding = strokeW + 8 * density
        val cx = width / 2f
        val cy = height * 0.52f
        val radius = min(cx, cy) - padding

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        trackPaint.strokeWidth = strokeW
        trackPaint.color = 0xFF162035.toInt()

        glowPaint.strokeWidth = strokeW + 8 * density
        glowPaint.color = gaugeColor and 0x22FFFFFF.toInt()

        arcPaint.strokeWidth = strokeW
        arcPaint.color = gaugeColor

        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, trackPaint)

        if (currentProgress > 0f) {
            canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE * currentProgress, false, glowPaint)
            canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE * currentProgress, false, arcPaint)
        }

        tickPaint.strokeWidth = 2 * density
        tickPaint.color = 0xFF1E3050.toInt()
        for (i in 0..TICK_COUNT) {
            val angle = START_ANGLE + SWEEP_ANGLE * i / TICK_COUNT
            val rad = Math.toRadians(angle.toDouble())
            val outerR = radius + strokeW / 2 + 4 * density
            val innerR = radius + strokeW / 2 + 2 * density
            canvas.drawLine(
                (cx + innerR * cos(rad)).toFloat(), (cy + innerR * sin(rad)).toFloat(),
                (cx + outerR * cos(rad)).toFloat(), (cy + outerR * sin(rad)).toFloat(),
                tickPaint
            )
        }

        val displaySpeed = if (speedValue >= 1000f) speedValue / 1000f else speedValue
        val speedText = if (displaySpeed >= 100f) "%.0f".format(displaySpeed)
                        else "%.1f".format(displaySpeed)
        val unitText = if (speedValue >= 1000f) "Gbps" else "Mbps"

        speedTextPaint.textSize = radius * 0.52f
        speedTextPaint.color = gaugeColor
        canvas.drawText(speedText, cx, cy + speedTextPaint.textSize * 0.32f, speedTextPaint)

        unitTextPaint.textSize = radius * 0.16f
        unitTextPaint.color = 0xFF4E6A8E.toInt()
        unitTextPaint.letterSpacing = 0.2f
        canvas.drawText(unitText, cx, cy + speedTextPaint.textSize * 0.72f, unitTextPaint)

        labelTextPaint.textSize = radius * 0.13f
        labelTextPaint.color = 0xFF4E6A8E.toInt()
        labelTextPaint.letterSpacing = 0.15f
        canvas.drawText(label.uppercase(), cx, cy + speedTextPaint.textSize * 1.05f, labelTextPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, (w * 0.75f).toInt())
    }
}
