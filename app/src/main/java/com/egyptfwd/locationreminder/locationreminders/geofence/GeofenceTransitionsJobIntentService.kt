package com.egyptfwd.locationreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.egyptfwd.locationreminder.R
import com.egyptfwd.locationreminder.locationreminders.data.ReminderDataSource
import com.google.android.gms.location.Geofence
import com.egyptfwd.locationreminder.locationreminders.data.dto.ReminderDTO
import com.egyptfwd.locationreminder.locationreminders.data.dto.Result
import com.egyptfwd.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.egyptfwd.locationreminder.utils.sendNotification
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "GeofenceReceiver"


class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

//      TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification


        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(applicationContext, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        if (geofencingEvent != null) {
            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, applicationContext.getString(R.string.geofence_entered))
                sendNotification(geofencingEvent.triggeringGeofences as List<Geofence>)
            }
        }


    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        triggeringGeofences.forEach {

            val requestId = it.requestId

            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()

            //Interaction to the repository has to be through a coroutine scope
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
}
