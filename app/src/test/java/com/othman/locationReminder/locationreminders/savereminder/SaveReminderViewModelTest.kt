package com.othman.locationReminder.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.othman.locationReminder.locationreminders.data.FakeDataSource
import com.othman.locationReminder.locationreminders.reminderslist.MainCoroutineRule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import com.othman.locationReminder.R
import com.othman.locationReminder.locationreminders.reminderslist.ReminderDataItem
import com.othman.locationReminder.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var viewModel: SaveReminderViewModel
    var reminder = ReminderDataItem(
        "title",
        "description",
        "LocationTitle",
        30.12345,
        31.23456
    )

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun init() {
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeDataSource()
        )
    }


    @Test
    fun titleIsEmpty() {
        assertEquals(false,viewModel.validateEnteredData(reminder))
        assertEquals(R.string.err_enter_title,viewModel.showSnackBarInt.getOrAwaitValue())
    }

    @Test
    fun DidnotPickTheLocation() {
        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun checkLoading() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

}