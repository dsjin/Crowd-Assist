package th.ac.kmitl.it.crowdassist.util

import android.view.View

interface SetButtonListenerCallback{
    interface ClickListener{
        fun setClick(listener: View.OnClickListener)
    }
    interface LongClickListener{
        fun setLongClick(listener: View.OnLongClickListener)
    }
}
