package com.android.base.adapter.recycler.paging

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.drakeet.multitype.DefaultLinker
import com.drakeet.multitype.DelegateNotFoundException
import com.drakeet.multitype.ItemViewBinder
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.Items
import com.drakeet.multitype.MutableTypes
import com.drakeet.multitype.OneToManyBuilder
import com.drakeet.multitype.OneToManyFlow
import com.drakeet.multitype.Type
import com.drakeet.multitype.Types
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class PagingMultiTypeAdapter<T : Any>(
    diffCallback: DiffUtil.ItemCallback<T>,
    mainDispatcher: CoroutineContext = Dispatchers.Main,
    workerDispatcher: CoroutineContext = Dispatchers.Default,
    initialTypeCapacity: Int = 0,
    private val types: Types = MutableTypes(initialTypeCapacity),
) : PagingDataAdapter<T, ViewHolder>(diffCallback, mainDispatcher, workerDispatcher), Items {

    private class EmptyViewHolder(context: Context) : ViewHolder(View(context))

    private val emptyType = Int.MAX_VALUE - 99

    /**
     * Registers a type class and its item view delegate. If you have registered the class,
     * it will override the original delegate(s). Note that the method is non-thread-safe
     * so that you should not use it in concurrent operation.
     *
     * Note that the method should not be called after
     * [RecyclerView.setAdapter], or you have to call the setAdapter
     * again.
     *
     * @param clazz the class of a item
     * @param delegate the item view delegate
     * @param T the item data type
     * */
    fun <T> register(clazz: Class<T>, delegate: ItemViewDelegate<T, *>) {
        unregisterAllTypesIfNeeded(clazz)
        register(Type(clazz, delegate, DefaultLinker()))
    }

    inline fun <reified T : Any> register(delegate: ItemViewDelegate<T, *>) {
        register(T::class.java, delegate)
    }

    inline fun <reified T : Any> register(
        // Keep this parameter to provide the explicit relationship
        @Suppress("UNUSED_PARAMETER") clazz: KClass<T>,
        delegate: ItemViewDelegate<T, *>,
    ) {
        // Always use the reified type to avoid javaPrimitiveType problem
        // See https://github.com/drakeet/MultiType/issues/302
        register(T::class.java, delegate)
    }

    fun <T> register(clazz: Class<T>, binder: ItemViewBinder<T, *>) {
        register(clazz, binder as ItemViewDelegate<T, *>)
    }

    inline fun <reified T : Any> register(binder: ItemViewBinder<T, *>) {
        register(binder as ItemViewDelegate<T, *>)
    }

    inline fun <reified T : Any> register(clazz: KClass<T>, binder: ItemViewBinder<T, *>) {
        register(clazz, binder as ItemViewDelegate<T, *>)
    }

    internal fun <T> register(type: Type<T>) {
        types.register(type)
        type.delegate._adapter = this
    }

    /**
     * Registers a type class to multiple item view delegates. If you have registered the
     * class, it will override the original delegate(s). Note that the method is non-thread-safe
     * so that you should not use it in concurrent operation.
     *
     * Note that the method should not be called after
     * [RecyclerView.setAdapter], or you have to call the setAdapter again.
     *
     * @param clazz the class of a item
     * @param <T> the item data type
     * @return [OneToManyFlow] for setting the delegates
     * @see [register]
     */
    @CheckResult
    fun <T> register(clazz: Class<T>): OneToManyFlow<T> {
        unregisterAllTypesIfNeeded(clazz)
        return OneToManyBuilder(clazz) {
            register(it)
        }
    }

    @CheckResult
    fun <T : Any> register(clazz: KClass<T>): OneToManyFlow<T> {
        return register(clazz.java)
    }

    /**
     * Registers all of the contents in the specified [Types]. If you have registered a
     * class, it will override the original delegate(s). Note that the method is non-thread-safe
     * so that you should not use it in concurrent operation.
     *
     * Note that the method should not be called after
     * [RecyclerView.setAdapter], or you have to call the setAdapter
     * again.
     *
     * @param types a [Types] containing contents to be added to this adapter inner [Types]
     * @see [register]
     * @see [register]
     */
    fun registerAll(types: Types) {
        val size = types.size
        for (i in 0 until size) {
            val type = types.getType<Any>(i)
            unregisterAllTypesIfNeeded(type.clazz)
            register(type)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItemSafely(position) ?: return emptyType
        return try {
            indexInTypesOf(position, item)
        } catch (e: DelegateNotFoundException) {
            Timber.d(e, "indexInTypesOf, position = $position snapshot.size = ${snapshot().size}, types.size = ${types.size}")
            emptyType
        }
    }

    private fun getItemSafely(position: Int): Any? {
        val item = try {
            getItem(position)
        } catch (e: IndexOutOfBoundsException) {
            Timber.d(e, "getItemViewType, position = $position snapshot.size = ${snapshot().size}")
        }
        return item
    }

    override fun onCreateViewHolder(parent: ViewGroup, indexViewType: Int): ViewHolder {
        if (indexViewType == emptyType) {
            return EmptyViewHolder(parent.context)
        }
        return types.getType<Any>(indexViewType).delegate.onCreateViewHolder(parent.context, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (holder.javaClass == EmptyViewHolder::class.java) {
            Timber.w("onBindViewHolder, but holder is EmptyViewHolder, position = $position")
            return
        }
        // We don't use holder.itemViewType here because it is not always correct especially when RecyclerView works with Paging.
        getItemSafely(position)?.let {
            getOutDelegateByPosition(position)?.onBindViewHolder(holder, it, payloads)
        }
    }

    /**
     * Called when a view created by this adapter has been recycled, and passes the event to its
     * associated delegate.
     *
     * @param holder The ViewHolder for the view being recycled
     * @see RecyclerView.Adapter.onViewRecycled
     * @see ItemViewDelegate.onViewRecycled
     */
    override fun onViewRecycled(holder: ViewHolder) {
        getOutDelegateByViewHolder(holder)?.onViewRecycled(holder)
    }

    /**
     * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
     * due to its transient state, and passes the event to its associated item view delegate.
     *
     * @param holder The ViewHolder containing the View that could not be recycled due to its
     * transient state.
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns `true`, RecyclerView *will ignore* the transient state of
     * the View and recycle it regardless. If this method returns `false`,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     * @see RecyclerView.Adapter.onFailedToRecycleView
     * @see ItemViewDelegate.onFailedToRecycleView
     */
    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return getOutDelegateByViewHolder(holder)?.onFailedToRecycleView(holder) ?: false
    }

    /**
     * Called when a view created by this adapter has been attached to a window, and passes the
     * event to its associated item view delegate.
     *
     * @param holder Holder of the view being attached
     * @see RecyclerView.Adapter.onViewAttachedToWindow
     * @see ItemViewDelegate.onViewAttachedToWindow
     */
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        getOutDelegateByViewHolder(holder)?.onViewAttachedToWindow(holder)
    }

    /**
     * Called when a view created by this adapter has been detached from its window, and passes
     * the event to its associated item view delegate.
     *
     * @param holder Holder of the view being detached
     * @see RecyclerView.Adapter.onViewDetachedFromWindow
     * @see ItemViewDelegate.onViewDetachedFromWindow
     */
    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        getOutDelegateByViewHolder(holder)?.onViewDetachedFromWindow(holder)
    }

    private fun getOutDelegateByViewHolder(holder: ViewHolder): ItemViewDelegate<Any, ViewHolder>? {
        // Don't use holder.bindingAdapterPosition here, because this Adapter maybe used with ConcatAdapter, which may cause
        // app to crash.  see [ConcatAdapterController#getLocalAdapterPosition].
        val itemViewType = holder.itemViewType
        if (itemViewType >= types.size) {
            Timber.w("getOutDelegateByViewHolder returned null, holder.itemViewType = $itemViewType, types.size = ${types.size} ")
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return types.getType<Any>(itemViewType).delegate as ItemViewDelegate<Any, ViewHolder>
    }

    private fun getOutDelegateByPosition(position: Int): ItemViewDelegate<Any, ViewHolder>? {
        val itemViewType = getItemViewType(position)
        if (itemViewType >= types.size) {
            Timber.w("getOutDelegateByPosition returned null, position = $position itemViewType = ${itemViewType}, types.size = ${types.size} ")
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return types.getType<Any>(itemViewType).delegate as ItemViewDelegate<Any, ViewHolder>
    }

    @Throws(DelegateNotFoundException::class)
    internal fun indexInTypesOf(position: Int, item: Any): Int {
        val index = types.firstIndexOf(item.javaClass)
        if (index != -1) {
            val linker = types.getType<Any>(index).linker
            return index + linker.index(position, item)
        }
        throw DelegateNotFoundException(item.javaClass)
    }

    private fun unregisterAllTypesIfNeeded(clazz: Class<*>) {
        if (types.unregister(clazz)) {
            Timber.w("The type ${clazz.simpleName} you originally registered is now overwritten.")
        }
    }

    override fun getItems(): List<Any> {
        return snapshot().items
    }

}