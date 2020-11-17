package com.what3words.autosuggest.util

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.what3words.autosuggest.SuggestionsListPosition

class MyDividerItemDecorator(
    private val mDivider: Drawable,
    private val position: SuggestionsListPosition
) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount =
            if (position == SuggestionsListPosition.BELOW) parent.childCount - 1 else parent.childCount
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