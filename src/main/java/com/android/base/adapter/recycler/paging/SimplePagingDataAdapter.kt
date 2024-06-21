package com.android.base.adapter.recycler.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.android.base.adapter.recycler.ViewBindingViewHolder
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * A simple way to build a simple list. If you want to build a multi type list, perhaps you need to use [PagingMultiTypeAdapter].
 *
 * @author Ztiany
 */
abstract class SimplePagingDataAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    mainDispatcher: CoroutineContext = Dispatchers.Main,
    workerDispatcher: CoroutineContext = Dispatchers.Default,
) : PagingDataAdapter<T, ViewBindingViewHolder<VB>>(diffCallback, mainDispatcher, workerDispatcher) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingViewHolder<VB> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewBindingViewHolder(provideViewBinding(parent, layoutInflater)).also {
            onViewHolderCreated(it)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewBindingViewHolder<VB>, position: Int) {
        val item = getItem(position)
        if (item != null) {
            onBindItem(viewHolder, item)
        } else {
            onBindNullItem(viewHolder, position)
        }
    }

    abstract fun provideViewBinding(parent: ViewGroup, inflater: LayoutInflater): VB

    protected open fun onViewHolderCreated(viewHolder: ViewBindingViewHolder<VB>) = Unit

    abstract fun onBindItem(viewHolder: ViewBindingViewHolder<VB>, item: T)

    protected open fun onBindNullItem(viewHolder: ViewBindingViewHolder<VB>, position: Int) = Unit

}