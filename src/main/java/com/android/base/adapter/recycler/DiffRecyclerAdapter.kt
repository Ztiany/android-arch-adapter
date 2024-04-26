package com.android.base.adapter.recycler

import android.content.Context
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.android.base.adapter.DataManager
import java.util.*
import java.util.concurrent.Executor

/**
 * 基于 DiffUtil 的 [RecyclerView.Adapter]。
 *
 * @param <T> 当前列表使用的数据类型
 * @author Ztiany
 */
abstract class DiffRecyclerAdapter<T, VH : RecyclerView.ViewHolder> @JvmOverloads constructor(
    protected val context: Context,
    itemCallback: DiffUtil.ItemCallback<T>,
    executor: Executor? = null,
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
        val newList = copyCurrent()
        newList.add(element)
        asyncListDiffer.submitList(newList)
    }

    override fun addAt(location: Int, element: T) {
        var position = location
        if (position > getDataSize()) {
            position = getDataSize()
        }
        val newList = copyCurrent()
        newList.add(position, element)
        asyncListDiffer.submitList(newList)
    }

    override fun addItems(elements: List<T>) {
        if (elements.isEmpty()) {
            return
        }
        val newList = copyCurrent()
        newList.addAll(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun addItemsChecked(elements: List<T>) {
        if (elements.isEmpty()) {
            return
        }
        val newList = copyCurrent()
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
        val newList = copyCurrent()
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
        val newList = copyCurrent()
        newList[newList.indexOf(oldElement)] = newElement
        asyncListDiffer.submitList(newList)
    }

    override fun replaceAt(index: Int, element: T) {
        if (getDataSize() > index) {
            val newList = copyCurrent()
            newList[index] = element
            asyncListDiffer.submitList(newList)
        }
    }

    override fun remove(element: T): Boolean {
        if (contains(element)) {
            val newList = copyCurrent()
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
        val newList = copyCurrent()
        newList.removeAll(elements)
        asyncListDiffer.submitList(newList)
    }

    override fun removeItems(elements: List<T>, isSuccessive: Boolean) {
        removeItems(elements)
    }

    override fun removeAt(index: Int) {
        if (getDataSize() > index) {
            val newList = copyCurrent()
            newList.removeAt(index)
            asyncListDiffer.submitList(newList)
        }
    }

    override fun clear() {
        asyncListDiffer.submitList(null)
    }

    override fun removeIf(filter: (T) -> Boolean) {
        val newList = copyCurrent()
        newList.removeAll(filter)
        asyncListDiffer.submitList(newList)
    }

    override fun swipePosition(fromPosition: Int, toPosition: Int) {
        val intRange = getList().indices
        if (fromPosition != toPosition && fromPosition in intRange && toPosition in intRange) {
            val newList = copyCurrent()
            Collections.swap(newList, fromPosition, toPosition)
            submitList(newList)
        }
    }

    private fun copyCurrent(): MutableList<T> {
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