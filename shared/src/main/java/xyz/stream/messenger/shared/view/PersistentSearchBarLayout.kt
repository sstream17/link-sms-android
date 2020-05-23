package xyz.stream.messenger.shared.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

class PersistentSearchBarLayout : LinearLayout {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    init {
        z = 1F
    }

    class ScrollingViewBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {
        override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
            return if (dependency is PersistentSearchBarLayout) {
                true
            } else {
                return super.layoutDependsOn(parent, child, dependency)
            }
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
            if (dependency is PersistentSearchBarLayout) {
                child.translationY = dependency.getY()
                return true
            }

            return super.onDependentViewChanged(parent, child, dependency)
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            Log.i("yeet", "scrolling behavior scroll")
            return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }
    }
}