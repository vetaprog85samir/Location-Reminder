package com.egyptfwd.locationreminder

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.egyptfwd.locationreminder.locationreminders.RemindersActivity
import com.egyptfwd.locationreminder.locationreminders.ToastMatcher
import com.egyptfwd.locationreminder.locationreminders.data.ReminderDataSource
import com.egyptfwd.locationreminder.locationreminders.data.dto.ReminderDTO
import com.egyptfwd.locationreminder.locationreminders.data.local.LocalDB
import com.egyptfwd.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.egyptfwd.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.egyptfwd.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.egyptfwd.locationreminder.util.DataBindingIdlingResource
import com.egyptfwd.locationreminder.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest: AutoCloseKoinTest() {
// Extended Koin Test -
// embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun after() {
        stopKoin()
    }


//    TODO: add End to End testing to the app

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }
    @Test
    fun createReminder_NoLocation_showSnackbar() {
        Thread.sleep(1000)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("TITLE"))
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_select_location)))

        activityScenario.close()
    }

    @Test
    fun createReminder_noTitle_showSnackbar() {
        Thread.sleep(1000)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))

        activityScenario.close()
    }


    @Test
    fun saveReminderScreen_showToastMessage() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("title"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("description"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())


        Espresso.onView(withText(R.string.reminder_saved))
            .inRoot(ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }


    @Test
    fun addReminder() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("Remember to do your morning training"))
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("Running, Push up"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.map)).perform(ViewActions.click())
        Espresso.pressBack()

        repository.saveReminder(
            ReminderDTO(
                "Remember to do your morning training",
                "Running, Push up",
                "Garden",
                0.0,
                0.0
            )
        )
        Espresso.onView(ViewMatchers.withText("Remember to do your morning training")).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("Running, Push up")).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(1000)
        activityScenario.close()
    }

}
