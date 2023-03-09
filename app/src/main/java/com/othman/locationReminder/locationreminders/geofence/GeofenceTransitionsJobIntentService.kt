package com.othman.locationReminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.othman.locationReminder.R
import com.othman.locationReminder.locationreminders.data.ReminderDataSource
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.data.dto.Result
import com.othman.locationReminder.locationreminders.data.local.RemindersLocalRepository
import com.othman.locationReminder.locationreminders.reminderslist.ReminderDataItem
import com.othman.locationReminder.locationreminders.savereminder.SaveReminderFragment
import com.othman.locationReminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TO DO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TO DO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e("onHandleWork", errorMessage)
                return
            }

            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                triggeringGeofences.forEach {
                    sendNotification(it)
                }

            } else {
                Log.e(
                    "onReceive", "" +
                            geofenceTransition +
                            ",,,," +
                            getString(R.string.geofence_transition_invalid_type)
                )
            }
        }
        //TO DO call @sendNotification
    }

    //TO DO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofence: Geofence) {
        val requestId = triggeringGeofence.requestId

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
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
