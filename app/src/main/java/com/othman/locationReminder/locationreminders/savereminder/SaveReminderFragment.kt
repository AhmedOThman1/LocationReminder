package com.othman.locationReminder.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.othman.locationReminder.R
import com.othman.locationReminder.base.BaseFragment
import com.othman.locationReminder.base.NavigationCommand
import com.othman.locationReminder.databinding.FragmentSaveReminderBinding
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.othman.locationReminder.locationreminders.reminderslist.ReminderDataItem
import com.othman.locationReminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    lateinit var geofencingClient: GeofencingClient
    lateinit var newReminder: ReminderDataItem
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            Log.w("LATLNG", "" + _viewModel.latitude.value + "," + _viewModel.longitude.value)
            if (location == null || latitude == null || longitude == null) {
                Toast.makeText(context, "Pick location first!", Toast.LENGTH_SHORT).show()
            } else if (title!!.isEmpty() || description!!.isEmpty()) {
                Toast.makeText(context, "Reminder details are missing!", Toast.LENGTH_SHORT).show()
            } else {
                newReminder = ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude
                )
                checkPermissionsAndStartGeofencing()

//            TO DO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(newReminder.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                newReminder.latitude!!,
                newReminder.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            // Create the geofence.
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
        Log.w("HERE4", "addGeofenceForClue");

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added.
                Log.w("HERE5", "addOnSuccessListener");
                _viewModel.saveReminder(newReminder)
            }
            addOnFailureListener {
                // Failed to add geofences.
            }
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    @TargetApi(Build.VERSION_CODES.Q)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        context?.let {
            val foregroundLocationApproved = (
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ))
            val backgroundPermissionApproved =
                if (runningQOrLater) {
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                it, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                } else {
                    true
                }
            return foregroundLocationApproved && backgroundPermissionApproved
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d("Request permissions", "Request foreground only location permission")
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                permissionsArray,
                resultCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Toast.makeText(
                context,
                R.string.permission_denied_explanation,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        Log.w("HERE", "checkDeviceLocationSettingsAndStartGeofence $resolve");
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("GEO", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Toast.makeText(
                    context,
                    R.string.location_required_error, Toast.LENGTH_SHORT
                ).show()
            }
        }
        Log.w("HERE2", "checkDeviceLocationSettingsAndStartGeofence $resolve");
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("HERE3", "isSuccessful ${it.isSuccessful}");
                addGeofenceForClue()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 6001
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 6002
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 6003
private const val LOCATION_PERMISSION_INDEX = 0
private const val GEOFENCE_RADIUS_IN_METERS = 200f
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1