package com.example.sensor

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Speedometer with needle.
 *
 * Created by danon on 26.02.14.
 * @version 1.0
 * @author Anton Danshin [anton.danshin@frtk.ru](mailto:anton.danshin@frtk.ru)
 */

class SpeedometerGauge : View {
    private var maxSpeed = DEFAULT_MAX_SPEED
    var speed = 0.0
        set(speed) {
            var speed = speed
            require(speed >= 0) { "Non-positive value specified as a speed." }
            if (speed > maxSpeed) speed = maxSpeed
            field = speed
            invalidate()
        }
    private var defaultColor = Color.rgb(0, 0, 0)
    private var majorTickStep = DEFAULT_MAJOR_TICK_STEP
    private var minorTicks = DEFAULT_MINOR_TICKS
    private var labelConverter: LabelConverter? = null
    private var unitsText = ""
    private val ranges: MutableList<ColoredRange> =
        ArrayList()
    private var backgroundPaint: Paint? = null
    private var backgroundInnerPaint: Paint? = null
    private var maskPaint: Paint? = null
    private var needlePaint: Paint? = null
    private var ticksPaint: Paint? = null
    private var txtPaint: Paint? = null
    private var unitsPaint: Paint? = null
    private var colorLinePaint: Paint? = null
    private var labelTextSize = 0
    private var unitsTextSize = 0
    private var mMask: Bitmap? = null

    constructor(context: Context?) : super(context) {
        init()
        val density = resources.displayMetrics.density
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density))
        setUnitsTextSize(Math.round(DEFAULT_UNITS_TEXT_SIZE_DP * density))
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
        val density = resources.displayMetrics.density
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density))
        setUnitsTextSize(Math.round(DEFAULT_UNITS_TEXT_SIZE_DP * density))
    }

    fun getMaxSpeed(): Double {
        return maxSpeed
    }

    fun setMaxSpeed(maxSpeed: Double) {
        require(maxSpeed > 0) { "Non-positive value specified as max speed." }
        this.maxSpeed = maxSpeed
        invalidate()
    }

    fun getUnitsText(): String {
        return unitsText
    }

    fun setUnitsText(unitsText: String) {
        this.unitsText = unitsText
        invalidate()
    }

    @TargetApi(11)
    fun setSpeed(
        progress: Double,
        duration: Long,
        startDelay: Long
    ): ValueAnimator {
        var progress = progress
        require(progress >= 0) { "Negative value specified as a speed." }
        if (progress > maxSpeed) progress = maxSpeed
        val va =
            ValueAnimator.ofObject(
                TypeEvaluator<Double> { fraction, startValue, endValue -> startValue + fraction * (endValue - startValue) },
                java.lang.Double.valueOf(speed),
                java.lang.Double.valueOf(progress)
            )
        va.duration = duration
        va.startDelay = startDelay
        va.addUpdateListener { animation ->
            val value = animation.animatedValue as Double
            if (value != null) speed = value
        }
        va.start()
        return va
    }

    @TargetApi(11)
    fun setSpeed(progress: Double, animate: Boolean): ValueAnimator {
        return setSpeed(progress, 1500, 200)
    }

    fun getDefaultColor(): Int {
        return defaultColor
    }

    fun setDefaultColor(defaultColor: Int) {
        this.defaultColor = defaultColor
        invalidate()
    }

    fun getMajorTickStep(): Double {
        return majorTickStep
    }

    fun setMajorTickStep(majorTickStep: Double) {
        require(majorTickStep > 0) { "Non-positive value specified as a major tick step." }
        this.majorTickStep = majorTickStep
        invalidate()
    }

    fun getMinorTicks(): Int {
        return minorTicks
    }

    fun setMinorTicks(minorTicks: Int) {
        this.minorTicks = minorTicks
        invalidate()
    }

    fun getLabelConverter(): LabelConverter? {
        return labelConverter
    }

    fun setLabelConverter(labelConverter: LabelConverter?) {
        this.labelConverter = labelConverter
        invalidate()
    }

    fun clearColoredRanges() {
        ranges.clear()
        invalidate()
    }

    fun addColoredRange(begin: Double, end: Double, color: Int) {
        var begin = begin
        var end = end
        require(begin < end) { "Incorrect number range specified!" }
        if (begin < -5.0 / 160 * maxSpeed) begin = -5.0 / 160 * maxSpeed
        if (end > maxSpeed * (5.0 / 160 + 1)) end = maxSpeed * (5.0 / 160 + 1)
        ranges.add(ColoredRange(color, begin, end))
        invalidate()
    }

    fun getLabelTextSize(): Int {
        return labelTextSize
    }

    fun setLabelTextSize(labelTextSize: Int) {
        this.labelTextSize = labelTextSize
        if (txtPaint != null) {
            txtPaint!!.textSize = labelTextSize.toFloat()
            invalidate()
        }
    }

    fun getUnitsTextSize(): Int {
        return unitsTextSize
    }

    fun setUnitsTextSize(unitsTextSize: Int) {
        this.unitsTextSize = unitsTextSize
        if (unitsPaint != null) {
            unitsPaint!!.textSize = unitsTextSize.toFloat()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT)

        // Draw Metallic Arc and background
        drawBackground(canvas)

        // Draw Ticks and colored arc
        drawTicks(canvas)

        // Draw Needle
        drawNeedle(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width: Int
        var height: Int

        //Measure Width
        width = if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            widthSize
        } else {
            -1
        }

        //Measure Height
        height = if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            heightSize
        } else {
            -1
        }
        if (height >= 0 && width >= 0) {
            width = Math.min(height, width)
            height = width / 2
        } else if (width >= 0) {
            height = width / 2
        } else if (height >= 0) {
            width = height * 2
        } else {
            width = 0
            height = 0
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    private fun drawNeedle(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        val radius = oval.width() * 0.2f + 10
        val smallOval = getOval(canvas, 0.2f)
        val angle = 10 + (speed / getMaxSpeed() * 160).toFloat()
        canvas.drawLine(
            (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * smallOval.width() * 0.5f).toFloat(),
            (oval.centerY() - Math.sin(angle / 180 * Math.PI) * smallOval.width() * 0.5f).toFloat(),
            (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius).toFloat(),
            (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius).toFloat(),
            needlePaint!!
        )
        canvas.drawArc(smallOval, 180f, 180f, true, backgroundPaint!!)
    }

    private fun drawTicks(canvas: Canvas) {
        val availableAngle = 160f
        val majorStep = (majorTickStep / maxSpeed * availableAngle).toFloat()
        val minorStep = majorStep / (1 + minorTicks)
        val majorTicksLength = 30f
        val minorTicksLength = majorTicksLength / 2
        val oval = getOval(canvas, 1f)
        val radius = oval.width() * 0.35f
        var currentAngle = 10f
        var curProgress = 0.0
        while (currentAngle <= 170) {
            canvas.drawLine(
                (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (radius - majorTicksLength / 2)).toFloat(),
                (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (radius - majorTicksLength / 2)).toFloat(),
                (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI) * (radius + majorTicksLength / 2)).toFloat(),
                (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI) * (radius + majorTicksLength / 2)).toFloat(),
                ticksPaint!!
            )
            for (i in 1..minorTicks) {
                val angle = currentAngle + i * minorStep
                if (angle >= 170 + minorStep / 2) {
                    break
                }
                canvas.drawLine(
                    (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius).toFloat(),
                    (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius).toFloat(),
                    (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)).toFloat(),
                    (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)).toFloat(),
                    ticksPaint!!
                )
            }
            if (labelConverter != null) {
                canvas.save()
                canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY())
                val txtX = oval.centerX() + radius + majorTicksLength / 2 + 8
                val txtY = oval.centerY()
                canvas.rotate(+90f, txtX, txtY)
                canvas.drawText(
                    labelConverter!!.getLabelFor(curProgress, maxSpeed)!!,
                    txtX,
                    txtY,
                    txtPaint!!
                )
                canvas.restore()
            }
            currentAngle += majorStep
            curProgress += majorTickStep
        }
        val smallOval = getOval(canvas, 0.7f)
        colorLinePaint!!.color = defaultColor
        canvas.drawArc(smallOval, 185f, 170f, false, colorLinePaint!!)
        for (range in ranges) {
            colorLinePaint!!.color = range.color
            canvas.drawArc(
                smallOval,
                (190 + range.begin / maxSpeed * 160).toFloat(),
                ((range.end - range.begin) / maxSpeed * 160).toFloat(),
                false,
                colorLinePaint!!
            )
        }
    }

    private fun getOval(canvas: Canvas, factor: Float): RectF {
        val oval: RectF
        val canvasWidth = canvas.width - paddingLeft - paddingRight
        val canvasHeight = canvas.height - paddingTop - paddingBottom
        oval = if (canvasHeight * 2 >= canvasWidth) {
            RectF(0f, 0f, canvasWidth * factor, canvasWidth * factor)
        } else {
            RectF(0f, 0f, canvasHeight * 2 * factor, canvasHeight * 2 * factor)
        }
        oval.offset(
            (canvasWidth - oval.width()) / 2 + paddingLeft,
            (canvasHeight * 2 - oval.height()) / 2 + paddingTop
        )
        return oval
    }

    private fun drawBackground(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        canvas.drawArc(oval, 180f, 180f, true, backgroundPaint!!)
        val innerOval = getOval(canvas, 0.9f)
        canvas.drawArc(innerOval, 180f, 180f, true, backgroundInnerPaint!!)
        val mask = Bitmap.createScaledBitmap(
            mMask!!,
            (oval.width() * 1.1).toInt(),
            (oval.height() * 1.1).toInt() / 2,
            true
        )
        canvas.drawBitmap(
            mask,
            oval.centerX() - oval.width() * 1.1f / 2,
            oval.centerY() - oval.width() * 1.1f / 2,
            maskPaint
        )
        canvas.drawText(unitsText, oval.centerX(), oval.centerY() / 1.5f, unitsPaint!!)
    }

    private fun init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint!!.style = Paint.Style.FILL
//        backgroundPaint!!.color = Color.rgb(127, 127, 127)
        backgroundPaint!!.color = resources.getColor(R.color.customBlack)
        backgroundInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundInnerPaint!!.style = Paint.Style.FILL
//        backgroundInnerPaint!!.color = Color.rgb(150, 150, 150)
        backgroundInnerPaint!!.color = resources.getColor(R.color.customDarkGray)
        txtPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        txtPaint!!.color = Color.WHITE
        txtPaint!!.textSize = labelTextSize.toFloat()
        txtPaint!!.textAlign = Paint.Align.CENTER
        txtPaint!!.isLinearText = true
        unitsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        unitsPaint!!.color = Color.WHITE
        unitsPaint!!.textSize = unitsTextSize.toFloat()
        unitsPaint!!.textAlign = Paint.Align.CENTER
        unitsPaint!!.isLinearText = true
        mMask = BitmapFactory.decodeResource(resources, R.drawable.spot_mask)
        mMask = Bitmap.createBitmap(mMask!!, 0, 0, mMask!!.getWidth(), mMask!!.getHeight() / 2)
        maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskPaint!!.isDither = true
        ticksPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        ticksPaint!!.strokeWidth = 3.0f
        ticksPaint!!.style = Paint.Style.STROKE
        ticksPaint!!.color = defaultColor
        colorLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        colorLinePaint!!.style = Paint.Style.STROKE
        colorLinePaint!!.strokeWidth = 5f
        colorLinePaint!!.color = defaultColor
        needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        needlePaint!!.strokeWidth = 5f
        needlePaint!!.style = Paint.Style.STROKE
        needlePaint!!.color = Color.argb(200, 255, 0, 0)
    }

    interface LabelConverter {
        fun getLabelFor(progress: Double, maxProgress: Double): String?
    }

    class ColoredRange(var color: Int, var begin: Double, var end: Double)

    companion object {
        private val TAG = SpeedometerGauge::class.java.simpleName
        const val DEFAULT_MAX_SPEED = 100.0
        const val DEFAULT_MAJOR_TICK_STEP = 20.0
        const val DEFAULT_MINOR_TICKS = 1
        const val DEFAULT_LABEL_TEXT_SIZE_DP = 12
        const val DEFAULT_UNITS_TEXT_SIZE_DP = 24
    }
}