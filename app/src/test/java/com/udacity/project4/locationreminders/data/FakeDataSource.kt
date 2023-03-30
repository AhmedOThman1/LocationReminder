package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    //    TO DO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    override suspend fun getReminders(): com.udacity.project4.locationreminders.data.dto.Result<List<ReminderDTO>> {

        return if (shouldReturnError)
            com.udacity.project4.locationreminders.data.dto.Result.Error("failed to get Reminders")
        else
            com.udacity.project4.locationreminders.data.dto.Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): com.udacity.project4.locationreminders.data.dto.Result<ReminderDTO> {
        if (shouldReturnError) {
            return com.udacity.project4.locationreminders.data.dto.Result.Error("failed to get Reminders")
        } else {
            val result = reminders.find {
                it.id == id
            }
            if (result == null)
                return com.udacity.project4.locationreminders.data.dto.Result.Error("Reminder not found")
            else
                return com.udacity.project4.locationreminders.data.dto.Result.Success(result)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}