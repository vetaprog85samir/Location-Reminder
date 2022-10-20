package com.egyptfwd.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.egyptfwd.locationreminder.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun saveReminder() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "Market",
            description = "Don't forget to buy home stuff",
            location = "Giza",
            latitude = 30.000444997945714,
            longitude = 31.1712957959315775
        )
        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "Market",
            description = "Don't forget to buy home stuff",
            location = "Giza",
            latitude = 30.000444997945714,
            longitude = 31.1712957959315775
        )

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))

    }

    @Test
    fun emptyRemindersList() = runBlockingTest {
        val reminder = database.reminderDao().getReminderById("3")

        assertThat(reminder, CoreMatchers.nullValue())

    }

}