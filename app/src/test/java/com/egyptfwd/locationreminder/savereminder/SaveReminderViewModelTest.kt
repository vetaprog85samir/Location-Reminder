package com.egyptfwd.locationreminder.savereminder


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.egyptfwd.locationreminder.MainTestCoroutineRule
import com.egyptfwd.locationreminder.data.FakeDataSource
import com.egyptfwd.locationreminder.getOrAwaitValue
import com.egyptfwd.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.egyptfwd.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.egyptfwd.locationreminder.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainTestCoroutineRule()
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setUp() {
        stopKoin()
        datasource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(
                ApplicationProvider.getApplicationContext(),
                datasource
            )
    }

    @After
    fun endKoin(){
        stopKoin()
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem(
            "Test Title",
            "Test Description",
            "Test Location",
            0.0,
            0.0
        )
        saveReminderViewModel.saveReminder(
            reminderDataItem
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        val reminderDataItem = ReminderDataItem(
            null,
            "Test Description",
            "Test location",
            0.0,
            0.0
        )
        val isDataValid = saveReminderViewModel.validateEnteredData(reminderDataItem)
        MatcherAssert.assertThat(isDataValid, `is`(false))
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }


}