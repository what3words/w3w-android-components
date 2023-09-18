package com.what3words.components.text

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

/**
 * Utility object for positioning one Android View relative to another View using precise alignment options.
 *
 * The [ViewAnchorUtils] object provides a set of utilities for anchoring one Android View to another,
 * taking into account specified positioning options and spacing. The [anchor] method calculates and
 * adjusts the Y-coordinate of the calling View to position it either below, above, or aligned with
 * the target View.
 */
object ViewAnchorUtils {
    enum class Position {
        BELOW,
        ABOVE,
        ALIGN
    }

    /**
     * Positions the calling View relative to another View, considering the specified Position and spacing.
     *
     * This method calculates and set the Y-coordinate of the calling View
     * based on its relationship to the target View and the desired spacing. It adjusts the Y-coordinate
     * to position the calling View either below, above, or aligned with the target View.
     *
     * @param target The target View to which the calling View should be anchored.
     * @param position The desired Position relative to the target View (Position.BELOW, Position.ABOVE, or Position.ALIGN).
     * @param spacing The spacing (in pixels) to apply between the calling View and the target View.
     */
    fun View.anchor(target: View, position: Position, spacing: Int) {
        //fetches the anchor view visible bounds
        this.viewTreeObserver.addOnGlobalLayoutListener {
            val offsetViewBounds = Rect()

            // get the visible drawing bounds of current view
            this@anchor.getDrawingRect(offsetViewBounds)

            // gets the position of the current view, relative to the position of the target view inside the parent coordinates
            (target.parent as ViewGroup).offsetDescendantRectToMyCoords(target, offsetViewBounds)

            val updatedY = when (position) {
                Position.BELOW -> {
                    offsetViewBounds.top.toFloat() + target.height + spacing
                }
                Position.ABOVE -> {
                    offsetViewBounds.top.toFloat() - target.height + spacing
                }
                Position.ALIGN -> {
                    offsetViewBounds.top.toFloat() + spacing
                }
            }

            this@anchor.y = updatedY
            this@anchor.x = target.x
        }
    }
}