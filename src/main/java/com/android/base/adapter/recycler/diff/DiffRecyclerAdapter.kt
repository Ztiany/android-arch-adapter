package com.android.base.adapter.recycler.diff

import android.content.Context
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.android.base.adapter.DataManager
import java.util.Collections
import java.util.concurrent.Executor

/**
 * A [RecyclerView.Adapter] based on [AsyncListDiffer].
 *
 * @author Ztiany
 */
abstract class DiffRecyclerAdapter<T, VH : RecyclerView.ViewHolder> @JvmOverloads constructor(
    protected val context: Context,
    itemCallback: DiffUtil.ItemCallback<T>,
    /** Normally, you don't need to provide a [Executor]. */
    executor: Executor? = null,
    /** The count of items as the list's header. */
    private val headerCount: Int = 0,
) : RecyclerView.Adapter<VH>(), DataManager<T> {

    private val asyncListDiffer: AsyncListDiffer<T>

    init {
        if (headerCount < 0) {
            throw IllegalArgumentException("headerCount can not be a negative number.")
        }
        val builder = AsyncDifferConfig.Builder(itemCallback)
        if (executor != null) {
            builder.setBackgroundThreadExecutor(executor)
        }
        val differConfig = builder.build()
        asyncListDiffer = AsyncListDiffer(newListCallback(), differConfig)
    }

    private fun newListCallback() = AdapterListUpdateCallback(this)

    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        if (headerCount != 0) {
            super.registerAdapterDataObserver(AdapterDataObserverProxy(observer, headerCount))
        } else {
            super.registerAdapterDataObserver(observer)
        }
    }

    override fun notifyElementChanged(element: T) {
        val position = indexItem(element)
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    override fun add(element: T) {
        val newList = snapshot()
        newList.add(element)
        asyncListDiffer.submitList(newList)
    }

    override fun addAt(location: Int, element: T) {
        var position = location
        if (position > getDataSize()) {
            position = getDataSize()
        }
        val newList = snapshot()
        newList.add(position, element)
        asyncListDiffer.submitList(newList)
    }

    override fun addItems(elements: List<T>) {
        if (elements.isEmpty()) {
            return
        }
        val newList = snapshot()
        newList.addAll(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun addItemsDistinguished(elements: List<T>) {
        if (elements.isEmpty()) {
            return
        }
        val newList = snapshot()
        for (element in elements) {
            if (element == null) {
                continue
            }
            newList.remove(element)
        }
        newList.addAll(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun addItemsAt(location: Int, elements: List<T>) {
        var realLocation = location
        if (elements.isEmpty()) {
            return
        }
        val newList = snapshot()
        if (realLocation > newList.size) {
            realLocation = newList.size
        }
        newList.addAll(realLocation, elements)
        asyncListDiffer.submitList(newList)
    }

    override fun replace(oldElement: T, newElement: T) {
        if (!contains(oldElement)) {
            return
        }
        val newList = snapshot()
        newList[newList.indexOf(oldElement)] = newElement
        asyncListDiffer.submitList(newList)
    }

    override fun replaceAt(index: Int, element: T) {
        if (getDataSize() > index) {
            val newList = snapshot()
            newList[index] = element
            asyncListDiffer.submitList(newList)
        }
    }

    override fun remove(element: T): Boolean {
        if (contains(element)) {
            val newList = snapshot()
            newList.remove(element)
            asyncListDiffer.submitList(newList)
            return true
        }
        return false
    }

    override fun removeItems(elements: List<T>) {
        if (elements.isEmpty() || isEmpty() || !getList().containsAll(elements)) {
            return
        }
        val newList = snapshot()
        newList.removeAll(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun removeItems(elements: List<T>, isSuccessive: Boolean) {
        removeItems(elements)
    }

    override fun removeAt(index: Int) {
        if (getDataSize() > index) {
            val newList = snapshot()
            newList.removeAt(index)
            asyncListDiffer.submitList(newList)
        }
    }

    override fun clear() {
        asyncListDiffer.submitList(null)
    }

    override fun removeIf(filter: (T) -> Boolean) {
        val newList = snapshot()
        newList.removeAll(filter)
        asyncListDiffer.submitList(newList)
    }

    override fun swipePosition(fromPosition: Int, toPosition: Int) {
        val intRange = getList().indices
        if (fromPosition != toPosition && fromPosition in intRange && toPosition in intRange) {
            val newList = snapshot()
            Collections.swap(newList, fromPosition, toPosition)
            submitList(newList)
        }
    }

    private fun snapshot(): MutableList<T> {
        return mutableListOf<T>().apply {
            addAll(getList())
        }
    }

    override fun indexItem(element: T): Int {
        return if (isEmpty()) -1 else getList().indexOf(element)
    }

    override fun getItem(position: Int): T? {
        return if (getDataSize() > position) getList()[position] else null
    }

    override fun contains(element: T): Boolean {
        return !isEmpty() && getList().contains(element)
    }

    override fun getItemCount(): Int {
        return getDataSize()
    }

    override fun isEmpty(): Boolean {
        return getList().isEmpty()
    }

    override fun getList(): List<T> {
        return asyncListDiffer.currentList
    }

    override fun getDataSize(): Int {
        return getList().size
    }

    override fun replaceAll(elements: List<T>) {
        val newList: List<T> = ArrayList(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun setDataSource(newDataSource: MutableList<T>) {
        asyncListDiffer.submitList(newDataSource)
    }

}

fun <T, VH : RecyclerView.ViewHolder> DiffRecyclerAdapter<T, VH>.submitList(list: List<T>) {
    setDataSource(list.toMutableList())
}

/**
 * refer to [Android 官方架构组件 Paging-Ex: 为分页列表添加 Header 和 Footer](https://juejin.im/post/6844903814189826062) for details.
 */
private class AdapterDataObserverProxy(private val adapterDataObserver: AdapterDataObserver, private val headerCount: Int) : AdapterDataObserver() {

    override fun onChanged() {
        adapterDataObserver.onChanged()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        adapterDataObserver.onItemRangeChanged(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        adapterDataObserver.onItemRangeChanged(positionStart + headerCount, itemCount, payload)
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        adapterDataObserver.onItemRangeInserted(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        adapterDataObserver.onItemRangeRemoved(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition + headerCount, toPosition + headerCount, itemCount)
    }

}