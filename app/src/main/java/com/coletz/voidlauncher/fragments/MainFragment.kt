package com.coletz.voidlauncher.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coletz.voidlauncher.databinding.FragmentMainBinding
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.mvvm.AppViewModel
import com.coletz.voidlauncher.utils.Accessible
import com.coletz.voidlauncher.utils.SpaceItemDecoration
import com.coletz.voidlauncher.views.AppsAdapter

class MainFragment: Fragment() {

    private val appViewModel: AppViewModel by activityViewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val appsAdapter = AppsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentMainBinding.inflate(inflater, container, false)
            .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.screenOffBtn.setOnClickListener { Accessible.screenOff(context) }
        binding.screenOffBtn.setOnLongClickListener { Accessible.openPowerDialog(context); true }

        binding.openNotificationBtn.setOnClickListener { Accessible.openNotification(context) }
        binding.openNotificationBtn.setOnLongClickListener { Accessible.openQuickSettings(context); true }

        binding.appsList.addItemDecoration(SpaceItemDecoration(58))

        appsAdapter.onAppClicked = {  app ->
            app.launch(context, onError = {
                Log.e("Error", "Error launching app", it)
                Toast.makeText(context, "Error launching app", Toast.LENGTH_LONG).show()
                appViewModel.updateApps()
            })
        }

        binding.appsList.adapter = appsAdapter

        setFavouriteApps()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setFavouriteApps(){
        listOf(
            AppEntity("Telefono", Intent.ACTION_DIAL, isIntent = true),
            AppEntity("com.android.chrome", "Chrome"),
            AppEntity("org.telegram.messenger", "Telegram"),
            AppEntity("com.twitter.android", "Twitter")
        ).let(appsAdapter::updateApps)
    }
}