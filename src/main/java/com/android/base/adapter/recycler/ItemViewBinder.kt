package com.android.base.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.drakeet.multitype.ItemViewBinder

/**
 * @author Ztiany
 */
abstract class SimpleItemViewBinder<T, VB : ViewBinding> : ItemViewBinder<T, BindingViewHolder<VB>>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): BindingViewHolder<VB> {
        return BindingViewHolder(provideViewBinding(inflater, parent)).apply {
            onViewHolderCreated(this)
        }
    }

    protected open fun onViewHolderCreated(viewHolder: BindingViewHolder<VB>) = Unit

    abstract fun provideViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB

}