package com.egyptfwd.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.egyptfwd.locationreminder.locationreminders.MainAndroidTestCoroutineRule
import com.egyptfwd.locationreminder.locationreminders.data.dto.ReminderDTO
import com.egyptfwd.locationreminder.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //TODO: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun createRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO,
                Dispatchers.Main
            )
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun saveReminder() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "Market",
            description = "Don't forget to buy home stuff",
            location = "Giza",
            latitude = 30.000444997945714,
            longitude = 31.1712957959315775
        )
        repository.saveReminder(reminder)
        val reminderLoaded = repository.getReminder(reminder.id) as Result.Success<ReminderDTO>
        val loaded = reminderLoaded.data

        assertThat(loaded, Matchers.notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "Market",
            description = "Don't forget to buy home stuff",
            location = "Giza",
            latitude = 30.000444997945714,
            longitude = 31.1712957959315775
        )
        repository.saveReminder(reminder)
        repository.deleteAllReminders()
        val reminders = repository.getReminders() as Result.Success<List<ReminderDTO>>
        val data = reminders.data
        assertThat(data.isEmpty(), `is`(true))

    }

    @Test
    fun emptyRemindersList() = mainCoroutineRule.runBlockingTest {
        val reminder = repository.getReminder("3") as Result.Error
        assertThat(reminder.message, Matchers.notNullValue())
        assertThat(reminder.message, `is`("Reminder not found!"))
    }
}