package com.android.base.adapter.list

import androidx.viewbinding.ViewBinding

/**
 *@author Ztiany
 */
open class ViewBindingViewHolder<VB : ViewBinding>(
    val vb: VB,
) : ViewHolder(vb.root) {

    fun withVB(action: VB.() -> Unit) {
        with(vb, action)
    }

}
