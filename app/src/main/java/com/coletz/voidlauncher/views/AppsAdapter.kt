package com.coletz.voidlauncher.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coletz.voidlauncher.databinding.AppListItemBinding
import com.coletz.voidlauncher.models.AppEntity
import kotlin.collections.ArrayList

class AppsAdapter: ListAdapter<AppEntity, AppsAdapter.Holder>(Differ) {

    var onAppClicked: (AppEntity) -> Unit = {}

    var onAppLongClicked: (AppEntity) -> Boolean = { false }

    var onVisibleAppsLoaded: () -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(AppListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bind(getItem(position))
    }

    fun updateApps(newApps: List<AppEntity>) {
        submitList(newApps) { onVisibleAppsLoaded() }
    }

    inner class Holder(private val binding: AppListItemBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {

        private lateinit var app: AppEntity

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(app: AppEntity){
            this.app = app
            binding.itemLabel.text = app.uiName
        }

        override fun onClick(v: View?) {
            onAppClicked(app)
        }

        override fun onLongClick(v: View?): Boolean =
            onAppLongClicked(app)
    }

    object Differ: DiffUtil.ItemCallback<AppEntity>() {

        override fun areContentsTheSame(oldItem: AppEntity, newItem: AppEntity): Boolean {
            return oldItem.uiName == newItem.uiName
        }

        override fun areItemsTheSame(oldItem: AppEntity, newItem: AppEntity): Boolean {
            return oldItem.packageName == newItem.packageName
        }
    }
}