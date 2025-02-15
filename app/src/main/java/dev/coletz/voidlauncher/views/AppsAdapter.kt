package dev.coletz.voidlauncher.views

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.coletz.voidlauncher.R

class AppsAdapter: ListAdapter<MainListUiItem, AppsAdapter.Holder>(Differ) {

    var onAppClicked: (AppUiItem) -> Unit = {}

    var onFolderClicked: (FolderUiItem) -> Unit = {}

    var onAppLongClicked: (AppUiItem) -> Unit = {}

    var onVisibleAppsLoaded: () -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateApps(newApps: List<MainListUiItem>) {
        submitList(newApps) { onVisibleAppsLoaded() }
    }


    inner class Holder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        private lateinit var item: MainListUiItem
        private var icFavorite: Drawable? = ContextCompat.getDrawable(itemView.context, R.drawable.ic_favorite)

        private val itemLabel: TextView = itemView.findViewById(R.id.item_label)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(item: MainListUiItem){
            this.item = item
            itemLabel.text = item.uiName

            if (item.isFavorite) {
                itemLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, icFavorite, null)
            } else {
                itemLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }

        override fun onClick(v: View?) {
            when (item.itemType) {
                MainListUiItem.Type.APP -> onAppClicked(item as AppUiItem)
                MainListUiItem.Type.FOLDER -> onFolderClicked(item as FolderUiItem)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            when (item.itemType) {
                MainListUiItem.Type.APP -> onAppLongClicked(item as AppUiItem)
                MainListUiItem.Type.FOLDER -> {
                    Toast.makeText(v?.context, "", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            return true
        }
    }

    object Differ: DiffUtil.ItemCallback<MainListUiItem>() {

        override fun areContentsTheSame(oldItem: MainListUiItem, newItem: MainListUiItem): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }

        override fun areItemsTheSame(oldItem: MainListUiItem, newItem: MainListUiItem): Boolean {
            return oldItem.areItemsTheSame(newItem)
        }
    }
}