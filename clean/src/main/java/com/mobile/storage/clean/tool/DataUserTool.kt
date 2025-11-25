package com.mobile.storage.clean.tool

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class StringDelegate(private val value: String) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }
}

object DataUserTool {

    val String.Companion.upUrl: String by StringDelegate("https://test-pacific.pdfvieweredit.com/natchez/frye/sancho")

    val String.Companion.adminUrl: String by StringDelegate("https://djy.pdfvieweredit.com/apitest/vder/sss/")

    val String.Companion.pangKey: String by StringDelegate("8580262")

    val String.Companion.applyKey: String by StringDelegate("5MiZBZBjzzChyhaowfLpyR")
}