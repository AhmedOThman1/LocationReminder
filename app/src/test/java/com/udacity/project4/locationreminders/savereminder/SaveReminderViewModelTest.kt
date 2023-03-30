package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.MainCoroutineRule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var viewModel: SaveReminderViewModel
    var reminder = ReminderDataItem(
        "",
        "description",
        "",
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
            FakeDataSource(mutableListOf())
        )
    }


    @Test
    fun titleIsEmpty() {
        assertEquals(false,viewModel.validateEnteredData(reminder))
        assertEquals(R.string.err_enter_title,viewModel.showSnackBarInt.getOrAwaitValue())
    }

    @Test
    fun didNotPickTheLocation() {
        assertEquals(false,viewModel.validateEnteredData(reminder))
        assertEquals(R.string.err_select_location,viewModel.showSnackBarInt.getOrAwaitValue())
//        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
//        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun checkLoading() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)
//        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        assertEquals(true,viewModel.showLoading.getOrAwaitValue())
        mainCoroutineRule.resumeDispatcher()
//        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
        assertEquals(false,viewModel.showLoading.getOrAwaitValue())
    }

}