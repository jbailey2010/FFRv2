package com.devingotaswitch.rankings.extras

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.util.*

/**
 * An adapter class which has the exact same behavior as the
 * [ArrayAdapter] with the sole difference that it allows to be filtered
 * using the exact filter constraint and not use spaces as a word delimiter for
 * the data item.
 */
class FilterWithSpaceAdapter<T>(context: Context, dropdownId: Int, textViewResourceId: Int,
                                objects: MutableList<T>) : BaseAdapter(), Filterable {
    /**
     * Contains the list of objects that represent the data of this
     * ArrayAdapter. The content of this list is referred to as "the array" in
     * the documentation.
     */
    private var mObjects: MutableList<T>? = null

    /**
     * Lock used to modify the content of [.mObjects]. Any write operation
     * performed on the array should be synchronized on this lock. This lock is
     * also used by the filter (see [.getFilter] to make a synchronized
     * copy of the original array of data.
     */
    private val mLock = Any()

    /**
     * The resource indicating what views to inflate to display the content of
     * this array adapter.
     */
    private var mResource = 0

    /**
     * The resource indicating what views to inflate to display the content of
     * this array adapter in a drop down widget.
     */
    private var mDropDownResource = 0

    /**
     * If the inflated resource is not a TextView, mFieldId is used to
     * find a TextView inside the inflated views hierarchy. This field must
     * contain the identifier that matches the one defined in the resource file.
     */
    private var mFieldId = 0

    /**
     * Indicates whether or not [.notifyDataSetChanged] must be called
     * whenever [.mObjects] is modified.
     */
    private var mNotifyOnChange = true

    /**
     * Returns the context associated with this array adapter. The context is
     * used to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    var context: Context? = null
        private set

    // A copy of the original mObjects array, initialized from and then used
    // instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the
    // filtered values.
    private var mOriginalValues: ArrayList<T>? = null
    private var mFilter: ArrayFilter? = null
    private var mInflater: LayoutInflater? = null

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object
     * The object to add at the end of the array.
     */
    fun add(`object`: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.add(`object`)
            } else {
                mObjects!!.add(`object`)
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object
     * The object to insert into the array.
     * @param index
     * The index at which the object must be inserted.
     */
    fun insert(`object`: T, index: Int) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.add(index, `object`)
            } else {
                mObjects!!.add(index, `object`)
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object
     * The object to remove.
     */
    fun remove(`object`: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.remove(`object`)
            } else {
                mObjects!!.remove(`object`)
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Remove all elements from the list.
     */
    fun clear() {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.clear()
            } else {
                mObjects!!.clear()
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator
     * The comparator used to sort the objects contained in this
     * adapter.
     */
    fun sort(comparator: Comparator<in T>?) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator)
            } else {
                Collections.sort(mObjects, comparator)
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        mNotifyOnChange = true
    }

    private fun init(context: Context, resource: Int, textViewResourceId: Int,
                     objects: MutableList<T>) {
        this.context = context
        mInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDropDownResource = resource
        mResource = mDropDownResource
        mObjects = objects
        mFieldId = textViewResourceId
    }

    /**
     * {@inheritDoc}
     */
    override fun getCount(): Int {
        return mObjects!!.size
    }

    /**
     * {@inheritDoc}
     */
    override fun getItem(position: Int): T {
        return mObjects!![position]
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item
     * The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    fun getPosition(item: T): Int {
        return mObjects!!.indexOf(item)
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * {@inheritDoc}
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource)
    }

    private fun createViewFromResource(position: Int, convertView: View?,
                                       parent: ViewGroup, resource: Int): View {
        val text: TextView
        val view: View = convertView ?: mInflater!!.inflate(resource, parent, false)
        text = try {
            if (mFieldId == 0) {
                // If no custom field is assigned, assume the whole resource is
                // a TextView
                view as TextView
            } else {
                // Otherwise, find the TextView field within the layout
                view.findViewById(mFieldId)
            }
        } catch (e: ClassCastException) {
            Log.e("ArrayAdapter",
                    "You must supply a resource ID for a TextView")
            throw IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e)
        }
        val item = getItem(position)
        if (item is CharSequence) {
            text.text = item
        } else {
            text.text = item.toString()
        }
        return view
    }

    /**
     * {@inheritDoc}
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent,
                mDropDownResource)
    }

    /**
     * {@inheritDoc}
     */
    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter!!
    }

    /**
     *
     *
     * An array filter constrains the content of the array adapter with a
     * prefix. Each item that does not start with the supplied prefix is removed
     * from the list.
     *
     */
    private inner class ArrayFilter : Filter() {
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            if (mOriginalValues == null) {
                synchronized(mLock) { mOriginalValues = ArrayList(mObjects) }
            }
            if (prefix == null || prefix.isEmpty()) {
                var list: ArrayList<T>
                synchronized(mLock) { list = ArrayList(mOriginalValues) }
                results.values = list
                results.count = list.size
            } else {
                val prefixString = prefix.toString().toLowerCase(Locale.US)
                var values: ArrayList<T>
                synchronized(mLock) { values = ArrayList(mOriginalValues) }
                val count = values.size
                val newValues = ArrayList<T>()
                for (i in 0 until count) {
                    val value = values[i]
                    val valueText = value.toString().toLowerCase(Locale.US)

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value)
                        // see if it contains the constraint somewhere in the
                        // value element
                    } else if (valueText.contains(prefixString)) {
                        newValues.add(value)
                    }
                }
                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?,
                                    results: FilterResults) {
            // noinspection unchecked
            if (results.values != null) {
                mObjects = results.values as MutableList<T>
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    /**
     * Constructor
     *
     * @param context
     * The current context.
     * @param textViewResourceId
     * The resource ID for a layout file containing a TextView to use
     * when instantiating views.
     * @param objects
     * The objects to represent in the ListView.
     */
    init {
        init(context, dropdownId, textViewResourceId, objects)
    }
}