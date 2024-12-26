package com.naji.prayertimings.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naji.prayertimings.R
import com.naji.prayertimings.adapter.PrayerTimingsAdapter
import com.naji.prayertimings.databinding.ActivityMainBinding
import com.naji.prayertimings.view_model.activity.MainViewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var day = 0
    private var month = 0
    private var year = 0
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var postNotificationsPermissionARL: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDate()

        binding.apply {
            tvDate.text = "$day/${month+1}/$year"
            rvPrayerTimings.layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
        observeTimingLiveData()

        initPostNotificationsPermissionARL()
        requestPostNotificationsPermission()
    }

    private fun initPostNotificationsPermissionARL() {
        postNotificationsPermissionARL = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if(isGranted)
                Toast.makeText(this, getString(R.string.now_you_can_receive_notifications_whenever_a_prayer_time_comes), Toast.LENGTH_SHORT).show()
            else
                showNoNotificationsAbilityDialog()
        }
    }

    private fun showNoNotificationsAbilityDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.no_notifications))
            .setMessage(getString(R.string.you_have_to_give_us_the_permission_for_sending_you_permissions_whenever_a_prayer_time_comes))
            .setPositiveButton(getString(R.string.enable)) { dialogInterface, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                )
                dialogInterface.dismiss()
            }.setNegativeButton(getString(R.string.close)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.show()
    }

    private fun requestPostNotificationsPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
                postNotificationsPermissionARL.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun observeTimingLiveData() {
        viewModel.timingsLiveData.observe(this) { timings ->
            binding.apply {
                rvPrayerTimings.adapter = PrayerTimingsAdapter(ArrayList(timings))
                progressBar.isVisible = false
            }
        }
    }

    private fun initDate() {
        Calendar.getInstance().apply {
            day = get(Calendar.DAY_OF_MONTH)
            month = get(Calendar.MONTH)
            year = get(Calendar.YEAR)
        }
    }
}