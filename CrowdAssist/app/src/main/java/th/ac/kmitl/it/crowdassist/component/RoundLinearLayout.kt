package th.ac.kmitl.it.crowdassist.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.LinearLayout
import th.ac.kmitl.it.crowdassist.R
import kotlin.properties.Delegates

class RoundLinearLayout:LinearLayout{
    var radius = 20
    var path : Path by Delegates.notNull<Path>()
    constructor(context : Context): super(context)
    constructor(context: Context, attrs : AttributeSet):super(context, attrs){
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundLinearLayout)
        radius = a.getInt(R.styleable.RoundLinearLayout_radius, 20)
        a.recycle()
    }
    constructor(context : Context, attrs : AttributeSet, defStyleAttr : Int):super(context, attrs, defStyleAttr)
    init {
        path = Path()
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        val rect = RectF()
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        path.addRoundRect(rect, radius.toFloat(), radius.toFloat(), Path.Direction.CW)
        path.close()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        canvas.restoreToCount(save)
    }
}