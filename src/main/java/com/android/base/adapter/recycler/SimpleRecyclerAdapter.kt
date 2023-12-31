package com.android.base.adapter.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * A simple way to build a simple list. If you want to build a multi type list, perhaps you need to use [MultiTypeAdapter].
 *
 * @author Ztiany
 */
abstract class SimpleRecyclerAdapter<T, VB : ViewBinding>(
    context: Context,
    data: List<T> = emptyList()
) : RecyclerAdapter<T, BindingViewHolder<VB>>(context, data) {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<VB> {
        return BindingViewHolder(provideViewBinding(parent, inflater)).also {
            onViewHolderCreated(it)
        }
    }

    abstract fun provideViewBinding(parent: ViewGroup, inflater: LayoutInflater): VB

    override fun onBindViewHolder(viewHolder: BindingViewHolder<VB>, position: Int) {
        val item = getItem(position)
        if (item != null) {
            bindItem(viewHolder, item)
        } else {
            bindOnOverPosition(viewHolder, position)
        }
    }

    abstract fun bindItem(viewHolder: BindingViewHolder<VB>, item: T)

    protected open fun onViewHolderCreated(viewHolder: BindingViewHolder<VB>) = Unit

    protected open fun bindOnOverPosition(viewHolder: BindingViewHolder<VB>, position: Int) {

    }

}