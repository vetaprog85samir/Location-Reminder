package com.egyptfwd.locationreminder.data

import com.egyptfwd.locationreminder.locationreminders.data.ReminderDataSource
import com.egyptfwd.locationreminder.locationreminders.data.dto.ReminderDTO
import com.egyptfwd.locationreminder.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false
//    TODO: Create a fake data source to act as a double to the real data source
    //Done and passed as a parameter in the class

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //TODO("Return the reminders")
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("No reminders found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //TODO("save the reminder")
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //TODO("return the reminder with the id")
        if (shouldReturnError)
            return Result.Error("Test Exception")
        val found = reminders?.first { it.id == id }
        return if (found != null)
            Result.Success(found)
        else
            Result.Error("Reminder $id not found")
    }

    override suspend fun deleteAllReminders() {
        //TODO("delete all the reminders")
        reminders?.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


}