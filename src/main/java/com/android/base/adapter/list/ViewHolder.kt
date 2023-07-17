package com.android.base.adapter.list

import android.view.View
import com.android.base.adapter.ItemHelper

open class ViewHolder(val itemView: View) {

    var position = 0
    var type = 0

    private val helper: ItemHelper = ItemHelper(itemView)

    fun withHelper(black: ItemHelper.() -> Unit) {
        helper.black()
    }

}