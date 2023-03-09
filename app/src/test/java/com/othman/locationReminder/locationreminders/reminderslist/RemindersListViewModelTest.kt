package com.othman.locationReminder.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.othman.locationReminder.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Test
import com.othman.locationReminder.locationreminders.data.dto.Result
import com.othman.locationReminder.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    lateinit var fakeDataSource: FakeDataSource
    lateinit var viewModel: RemindersListViewModel
    var reminder = ReminderDTO(
        "title",
        "description",
        "LocationTitle",
        30.12345,
        31.23456
    )


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule= MainCoroutineRule()

    @Before
    fun setupViewModel(){
        fakeDataSource = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }
    //TODO: Error provide testing to the RemindersListViewModel and its live data objects

    @After
    fun close(){
        stopKoin()
    }


    @Test
    fun listIsNotEmpty() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.saveReminder(reminder)
        viewModel.loadReminders()
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun showLoading() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())

//        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertEquals(false, viewModel.showLoading.getOrAwaitValue())
//        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

}