package com.android.base.adapter.recycler.segment

import android.content.Context
import com.android.base.adapter.DataManager
import com.android.base.adapter.requireItem

open class MultiTypeAdapter : com.drakeet.multitype.MultiTypeAdapter, DataManager<Any> {

    protected val context: Context

    private var dataManager: RecyclerDataManagerImpl<Any>

    @Suppress("LeakingThis")
    constructor(context: Context, initialTypeCapacity: Int = 0) : super(initialCapacity = initialTypeCapacity) {
        this.context = context
        dataManager = RecyclerDataManagerImpl(mutableListOf(), this)
    }

    @Suppress("LeakingThis")
    constructor(context: Context, items: List<Any>, initialTypeCapacity: Int = 0) : super(initialCapacity = initialTypeCapacity) {
        this.context = context
        dataManager = RecyclerDataManagerImpl(items.toMutableList(), this)
    }

    override fun getItems(): List<Any> {
        return dataManager.getList()
    }

    override fun add(element: Any) {
        dataManager.add(element)
    }

    override fun addAt(location: Int, element: Any) {
        dataManager.addAt(location, element)
    }

    override fun addItems(elements: List<Any>) {
        dataManager.addItems(elements)
    }

    override fun addItemsDistinguished(elements: List<Any>) {
        dataManager.addItemsDistinguished(elements)
    }

    override fun addItemsAt(location: Int, elements: List<Any>) {
        dataManager.addItemsAt(location, elements)
    }

    override fun replace(oldElement: Any, newElement: Any) {
        dataManager.replace(oldElement, newElement)
    }

    override fun replaceAt(index: Int, element: Any) {
        dataManager.replaceAt(index, element)
    }

    override fun replaceAll(elements: List<Any>) {
        dataManager.replaceAll(elements)
    }

    override fun setDataSource(newDataSource: MutableList<Any>) {
        dataManager.setDataSource(newDataSource)
    }

    override fun remove(element: Any): Boolean {
        return dataManager.remove(element)
    }

    override fun removeAt(index: Int) {
        dataManager.removeAt(index)
    }

    override fun removeItems(elements: List<Any>) {
        dataManager.removeItems(elements)
    }

    override fun removeItems(elements: List<Any>, isSuccessive: Boolean) {
        dataManager.removeItems(elements, isSuccessive)
    }

    override fun getItem(position: Int): Any? {
        return dataManager.getItem(position)
    }

    override fun requireItem(position: Int): Any {
        return dataManager.requireItem(position)
    }

    override fun getItemCount(): Int {
        return dataManager.getDataSize()
    }

    override operator fun contains(element: Any): Boolean {
        return dataManager.contains(element)
    }

    override fun clear() {
        dataManager.clear()
    }

    override fun indexItem(element: Any): Int {
        return dataManager.indexItem(element)
    }

    override fun notifyElementChanged(element: Any) {
        dataManager.notifyElementChanged(element)
    }

    override fun isEmpty(): Boolean {
        return dataManager.isEmpty()
    }

    override fun getList(): List<Any> {
        return dataManager.getList()
    }

    override fun getDataSize(): Int {
        return dataManager.getDataSize()
    }

    override fun removeIf(filter: (Any) -> Boolean) {
        dataManager.removeIf(filter)
    }

    override fun swipePosition(fromPosition: Int, toPosition: Int) {
        dataManager.swipePosition(fromPosition, toPosition)
    }

}