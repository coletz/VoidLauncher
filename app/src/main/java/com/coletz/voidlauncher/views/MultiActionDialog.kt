package com.coletz.voidlauncher.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coletz.voidlauncher.databinding.AppOptionItemBinding

fun Fragment.multiActionDialog(configuration: MultiActionDialogOptions.() -> Unit) = requireContext().multiActionDialog(configuration)

fun Context.multiActionDialog(configuration: MultiActionDialogOptions.() -> Unit) {

    val options = MultiActionDialogOptions(this).apply(configuration)

    val actionsList = options.actions.toList()

    lateinit var alertDialog: AlertDialog

    val recyclerView = RecyclerView(this).apply {
        setPadding(0, 48, 0, 48)
        layoutManager = LinearLayoutManager(context)
        adapter = MultiActionAdapter(actionsList) {
            if (options.closeOnSelection) {
                alertDialog.dismiss()
            }
            it.action()
        }
    }

    alertDialog = AlertDialog.Builder(this).apply {
        options.title?.also { setTitle(it) }
        setView(recyclerView)
    }.show()
}

class MultiActionDialogOptions(private val context: Context) {

    var title: String? = null

    var closeOnSelection: Boolean = true

    internal val actions: MutableList<MultiActionDialogAction> = mutableListOf()

    fun add(label: String, action: () -> Unit) {
        actions.add(MultiActionDialogAction(label, action))
    }

    fun add(@StringRes labelResId: Int, action: () -> Unit) {
        actions.add(MultiActionDialogAction(context.getString(labelResId), action))
    }

    fun add(label: Context.() -> String, action: () -> Unit) {
        actions.add(MultiActionDialogAction(label(context), action))
    }
}

data class MultiActionDialogAction(val label: String, val action: () -> Unit)

private class MultiActionAdapter(
    actions: List<MultiActionDialogAction>,
    private val onClickListener: (MultiActionDialogAction) -> Unit
) : ListAdapter<MultiActionDialogAction, MultiActionAdapter.Holder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(AppOptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    init {
        submitList(actions)
    }

    inner class Holder(private val binding: AppOptionItemBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        private lateinit var item: MultiActionDialogAction

        fun bind(item: MultiActionDialogAction) {
            this.item = item
            binding.itemLabel.text = item.label
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onClickListener(item)
        }
    }

    private object Differ: DiffUtil.ItemCallback<MultiActionDialogAction>() {

        override fun areContentsTheSame(oldItem: MultiActionDialogAction, newItem: MultiActionDialogAction): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areItemsTheSame(oldItem: MultiActionDialogAction, newItem: MultiActionDialogAction): Boolean {
            return oldItem == newItem
        }
    }
}