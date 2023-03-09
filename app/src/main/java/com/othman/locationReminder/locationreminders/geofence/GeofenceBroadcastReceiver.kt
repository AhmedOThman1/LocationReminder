package com.othman.locationReminder.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.othman.locationReminder.R
import com.othman.locationReminder.locationreminders.data.ReminderDataSource
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.data.dto.Result
import com.othman.locationReminder.locationreminders.reminderslist.ReminderDataItem
import com.othman.locationReminder.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.othman.locationReminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import kotlin.coroutines.CoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob


    private lateinit var context: Context
    override fun onReceive(context: Context, intent: Intent) {
        //TO DO: implement the onReceive method to receive the geofencing events at the background
        this.context = context
        Log.w("HERE6", "onReceive");

//        if (intent.action == ACTION_GEOFENCE_EVENT) {
//            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
//        }

        if (intent.action == ACTION_GEOFENCE_EVENT) {

            Log.w("HERE7", "onReceive $intent.action");

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e("onReceive", errorMessage)
                return
            }


            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                Log.w("HERE8", "onReceive $geofenceTransition ${geofencingEvent.triggeringGeofences.size}");

                triggeringGeofences.forEach {
                    sendNotification(it)
                }
                // Get the transition details as a String.
//                val geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//                )

                // Send notification and log the transition details.
//                sendNotification(geofenceTransitionDetails)
//                Log.i("onReceive", geofenceTransitionDetails)
            } else {
                // Log the error.
                Log.e(
                    "onReceive", "" +
                            geofenceTransition +
                            ",,,," +
                            context.getString(R.string.geofence_transition_invalid_type)
                )
            }
        }


    }


    private val remindersLocalRepository: ReminderDataSource
            by inject(ReminderDataSource::class.java)

    private fun sendNotification(triggeringGeofence: Geofence) {
        val requestId = triggeringGeofence.requestId

        //Get the local repository instance
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    context, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}