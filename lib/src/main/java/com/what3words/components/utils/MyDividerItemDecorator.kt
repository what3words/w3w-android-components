package com.what3words.components.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class MyDividerItemDecorator(
    private val mDivider: Drawable?,
    private val verticalSpacing: Float,
    private val isReverse: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = verticalSpacing.toInt()
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = verticalSpacing.toInt()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if(mDivider == null) return
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount =
            if (!isReverse) parent.childCount - 1 else parent.childCount
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params =
                child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + mDivider.intrinsicHeight
            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            mDivider.draw(c)
        }
    }
}
