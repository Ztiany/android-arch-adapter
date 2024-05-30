package com.android.base.adapter.recycler.segment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.android.base.adapter.recycler.ViewBindingViewHolder

/**
 * A simple way to build a simple list. If you want to build a multi type list, perhaps you need to use [MultiTypeAdapter].
 *
 * @author Ztiany
 */
abstract class SimpleRecyclerAdapter<T, VB : ViewBinding>(
    context: Context,
    data: List<T> = emptyList(),
) : BaseRecyclerAdapter<T, ViewBindingViewHolder<VB>>(context, data) {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingViewHolder<VB> {
        return ViewBindingViewHolder(provideViewBinding(parent, inflater)).also {
            onViewHolderCreated(it)
        }
    }

    abstract fun provideViewBinding(parent: ViewGroup, inflater: LayoutInflater): VB

    override fun onBindViewHolder(viewHolder: ViewBindingViewHolder<VB>, position: Int) {
        val item = getItem(position)
        if (item != null) {
            bindItem(viewHolder, item)
        } else {
            onOverPosition(viewHolder, position)
        }
    }

    abstract fun bindItem(viewHolder: ViewBindingViewHolder<VB>, item: T)

    protected open fun onViewHolderCreated(viewHolder: ViewBindingViewHolder<VB>) = Unit

    protected open fun onOverPosition(viewHolder: ViewBindingViewHolder<VB>, position: Int) = Unit

}