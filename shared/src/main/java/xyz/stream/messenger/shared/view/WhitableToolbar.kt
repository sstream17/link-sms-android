package xyz.stream.messenger.shared.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.ColorUtils

@Suppress("NAME_SHADOWING")
class WhitableToolbar : MaterialToolbar {

    private var appliedBackgroundColor = Integer.MIN_VALUE
    private var titleView: TextView? = null

    val textColor: Int
        get() = if (appliedBackgroundColor == Integer.MIN_VALUE || ColorUtils.isColorDark(appliedBackgroundColor)) {
            Color.WHITE
        } else {
            resources.getColor(R.color.lightToolbarTextColor)
        }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun setTitle(title: CharSequence?) {
        ensureTitleView()
        titleView!!.text = title
    }

    override fun setBackgroundColor(color: Int) {
        val color = ActivityUtils.possiblyOverrideColorSelection(context, color)

        super.setBackgroundColor(color)
        this.appliedBackgroundColor = color

        val textColor = textColor
        val tintList = ColorStateList.valueOf(textColor)

        setTitleTextColor(textColor)
        if (overflowIcon != null) overflowIcon!!.setTintList(tintList)
        if (navigationIcon != null) navigationIcon!!.setTintList(tintList)
    }

    override fun setTitleTextColor(color: Int) {
        ensureTitleView()
        titleView!!.setTextColor(color)
    }

    override fun setNavigationIcon(res: Int) {
        super.setNavigationIcon(res)

        if (navigationIcon != null) {
            navigationIcon!!.setTintList(ColorStateList.valueOf(textColor))
        }
    }

    override fun inflateMenu(menu: Int) {
        super.inflateMenu(menu)

        (0 until getMenu().size())
                .filter { getMenu().getItem(it).icon != null }
                .forEach { getMenu().getItem(it).icon.setTintList(ColorStateList.valueOf(textColor)) }
    }

    fun alignTitleCenter() {
        ensureTitleView()
        val params = titleView!!.layoutParams as Toolbar.LayoutParams
        params.gravity = Gravity.CENTER
        titleView!!.layoutParams = params
    }

    fun alignTitleStart() {
        ensureTitleView()
        val params = titleView!!.layoutParams as Toolbar.LayoutParams
        params.gravity = Gravity.START
        titleView!!.layoutParams = params
    }

    private fun ensureTitleView() {
        titleView = findViewById(TOOLBAR_TITLE_ID)
        if (titleView == null) {
            titleView = TextView(context)
            titleView!!.id = TOOLBAR_TITLE_ID
            if (Build.VERSION.SDK_INT < 23) {
                titleView!!.setTextAppearance(context, android.R.style.TextAppearance_Material_Widget_ActionBar_Title)
            } else {
                titleView!!.setTextAppearance(android.R.style.TextAppearance_Material_Widget_ActionBar_Title)
            }

            titleView!!.gravity = Gravity.CENTER
            this.addView(titleView)
        }
    }

    companion object {
        const val TOOLBAR_TITLE_ID = 730016
    }
}
