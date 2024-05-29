package com.android.base.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.drakeet.multitype.ItemViewBinder

/**
 * @author Ztiany
 */
abstract class ViewBindingItemViewBinder<T, VB : ViewBinding> : ItemViewBinder<T, ViewBindingViewHolder<VB>>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewBindingViewHolder<VB> {
        return ViewBindingViewHolder(provideViewBinding(inflater, parent)).apply {
            onViewHolderCreated(this)
        }
    }

    protected open fun onViewHolderCreated(viewHolder: ViewBindingViewHolder<VB>) = Unit

    abstract fun provideViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB

}