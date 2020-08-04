package com.coletz.voidlauncher.views

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.models.AppObject
import com.coletz.voidlauncher.utils.AppsDiffUtils
import kotlin.collections.ArrayList

class AppsAdapter(private val recyclerView: RecyclerView): RecyclerView.Adapter<AppsAdapter.Holder>() {

    private val allApps: ArrayList<AppObject> = arrayListOf()
    private val filteredApps: ArrayList<AppObject> = arrayListOf()

    var filter: String = ""
        set(value) {
            field = value
            updateFilter()
        }

    var onAppClicked: (AppObject) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(TextView(parent.context))

    override fun getItemCount(): Int = filteredApps.size

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bind(filteredApps[position])
    }

    fun updateApps(newApps: List<AppObject>) {

        AppsDiffUtils.calculateDiff(allApps, newApps).dispatchUpdatesTo(this)

        allApps.clear()
        allApps.addAll(newApps)

        updateFilter()
    }

    private fun updateFilter(){
        val oldAppList = filteredApps.clone() as List<AppObject>

        val filterPredicate: (AppObject) -> Boolean = { (name) ->
            if (filter.isBlank()) {
                true
            } else {
                name
                    .split(" ")
                    .any { it.startsWith(filter, ignoreCase = true) }
            }
        }

        filteredApps.clear()
        filteredApps.addAll(allApps.filter(filterPredicate))


        val newAppList = filteredApps.sortedDescending()
        AppsDiffUtils.calculateDiff(oldAppList, newAppList).dispatchUpdatesTo(this)

        recyclerView.scrollToPosition(newAppList.size - 1)
    }

    inner class Holder(view: TextView): RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var app: AppObject

        private val appName = view

        init {
            appName.setOnClickListener(this)
            appName.setTextSize(TypedValue.COMPLEX_UNIT_PX, appName.context.resources.getDimension(
                R.dimen.app_name_size
            ))
            appName.setLines(1)
        }

        fun bind(app: AppObject){
            this.app = app
            appName.text = app.name
        }

        override fun onClick(v: View?) {
            onAppClicked(app)
        }
    }
}