package com.android.base.adapter.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.android.base.viewbinding.inflateBindingWithParameterizedType

abstract class SimpleListAdapter<T, VB : ViewBinding>(
    context: Context,
    data: MutableList<T> = mutableListOf(),
) : BaseListAdapter<T, ViewBindingViewHolder<VB>>(context, data) {

    override fun onCreateViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        type: Int
    ) = ViewBindingViewHolder(provideViewBinding(parent, layoutInflater)).also {
        onViewHolderCreated(it)
    }

    protected open fun provideViewBinding(parent: ViewGroup, inflater: LayoutInflater): VB {
        return inflateBindingWithParameterizedType(inflater, parent, false)
    }

    protected open fun onViewHolderCreated(viewHolder: ViewBindingViewHolder<VB>) = Unit

}