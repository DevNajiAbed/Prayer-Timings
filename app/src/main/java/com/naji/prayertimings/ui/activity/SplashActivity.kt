package com.naji.prayertimings.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naji.prayertimings.R
import com.naji.prayertimings.databinding.ActivitySplashBinding
import com.naji.prayertimings.util.MyUtil
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var locationPermissionsARL: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionsGranted = true
    /*private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i("nji", "here1")
            locationResult.lastLocation?.let { location ->
                fusedLocationProvider.removeLocationUpdates(this)
                val lat = location.latitude
                val lng = location.longitude
                MyUtil.saveLocationIntoPrefs(this@SplashActivity, LatLng(lat, lng))
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
            Log.i("nji", "here2")
        }
    }*/
    private val locationListener = LocationListener { location ->
        val lat = location.latitude
        val lng = location.longitude
        MyUtil.saveLocationIntoPrefs(this@SplashActivity, LatLng(lat, lng))
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        if (MyUtil.getLocationFromPrefs(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        setContentView(binding.root)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        initARLs()

        lifecycleScope.launch {
            if (!checkLocationPermissions()) {
                requestLocationPermissions()
            }
        }
    }

    private fun initARLs() {
        initLocationPermissionsARL()
    }

    override fun onStart() {
        super.onStart()
        if (!isLocationPermissionsGranted && !checkLocationPermissions())
            showFailureDialog()
        else if (!checkLocationSettingsEnabled())
            showLocationSettingsDisabledDialog()
        else if (isLocationPermissionsGranted && checkLocationPermissions() && checkLocationSettingsEnabled())
            getLocation()
    }

    private fun initLocationPermissionsARL() {
        locationPermissionsARL = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.contains(false)) {
                isLocationPermissionsGranted = false
                showFailureDialog()
            } else {
                isLocationPermissionsGranted = true
                binding.progressBar.isVisible = true
                getLocation()
            }
        }
    }

    private fun showFailureDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.failure))
            .setMessage(getString(R.string.you_have_to_allow_us_from_accessing_your_location_so_you_can_find_prayer_timings_in_your_city))
            .setPositiveButton(getString(R.string.retry)) { dialogInterface, _ ->
                goToLocationSettings()
                dialogInterface.dismiss()
            }.setNegativeButton(getString(R.string.close)) { _, _ ->
                finish()
            }.show()
    }

    private fun goToLocationSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }

    private fun requestLocationPermissions() {
        locationPermissionsARL.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun checkLocationPermissions(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun getLocation() {
        if (!checkLocationSettingsEnabled()) {
            showLocationSettingsDisabledDialog()
            return
        }
        fusedLocationProvider.apply {
            binding.progressBar.isVisible = true
            Toast.makeText(
                this@SplashActivity,
                getString(R.string.please_wait_it_might_take_a_few_minutes),
                Toast.LENGTH_SHORT
            ).show()
            lastLocation.addOnSuccessListener {
                if (it == null) {
                    requestNewLocationUpdates()
                    return@addOnSuccessListener
                }
                it.let { location ->
                    val lat = location.latitude
                    val lng = location.longitude
                    MyUtil.saveLocationIntoPrefs(this@SplashActivity, LatLng(lat, lng))
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationUpdates() {
        (getSystemService(LOCATION_SERVICE) as LocationManager)
            .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
    }

    private fun showLocationSettingsDisabledDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.location_turned_off))
            .setMessage(getString(R.string.you_have_to_enable_the_location_settings_in_your_device_so_we_can_get_prayer_timings_in_your_specific_location))
            .setPositiveButton(getString(R.string.enable)) { dialogInterface, _ ->
                if (!checkLocationSettingsEnabled())
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialogInterface.dismiss()
            }.setNegativeButton(getString(R.string.close)) { _, _ ->
                finish()
            }.show()
    }

    private fun checkLocationSettingsEnabled(): Boolean {
        (getSystemService(LOCATION_SERVICE) as LocationManager).apply {
            val isGPSEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            return isGPSEnabled && isNetworkEnabled
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(locationListener)
    }
}