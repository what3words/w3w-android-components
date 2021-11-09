package com.what3words.components.utils

import android.content.Context
import android.text.TextUtils

internal class FlagResourceTranslatorImpl(private val context: Context) : FlagResourceTranslator {

    companion object {
        private const val COUNTRY_FLAG_PREFIX = "ic_"
    }

    override fun translate(countryCode: String): Int {

        val res = context.resources
        var id = -1
        if (!TextUtils.isEmpty(countryCode)) {
            id = res.getIdentifier(COUNTRY_FLAG_PREFIX + countryCode.toLowerCase(), "drawable", context.packageName)
        }

        return id
    }
}
