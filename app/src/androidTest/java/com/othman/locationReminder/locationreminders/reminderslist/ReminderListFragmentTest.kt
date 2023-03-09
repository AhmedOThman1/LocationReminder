package com.othman.locationReminder.locationreminders.reminderslist


import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.othman.locationReminder.R
import com.othman.locationReminder.locationreminders.data.ReminderDataSource
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.data.local.LocalDB
import com.othman.locationReminder.locationreminders.data.local.RemindersLocalRepository
import com.othman.locationReminder.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.koin.test.get
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)

@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest(){

    private lateinit var dataSource: ReminderDataSource
    private lateinit var appContext: Application
    var reminder = ReminderDTO(
        "title",
        "description",
        "LocationTitle",
        30.12345,
        31.23456
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()



    //    TODO: error test the navigation of the fragments.
//    TO DO: test the displayed data on the UI.
//    TO DO: add testing for the error messages.
    @Before
    fun init() {
        stopKoin()
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
        startKoin {
            modules(listOf(myModule))
        }
        dataSource = get()
        runBlocking {
            dataSource.deleteAllReminders()
        }
    }


    @Test
    fun noDataTextExist() = runBlocking {
        dataSource.saveReminder(reminder)
        dataSource.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun listRecyclerExist()= runBlocking {
        dataSource.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    ViewMatchers.hasDescendant(ViewMatchers.withText(reminder.title))
                )
            )
    }


    // test the navigation of the fragments.
    @Test
    fun navigateToSaveReminderFragment () {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme).onFragment {
            Navigation.setViewNavController(it.view!!, mock(NavController::class.java))
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mock(NavController::class.java)).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}