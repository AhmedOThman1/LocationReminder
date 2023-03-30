package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import junit.framework.Assert.assertEquals
import com.udacity.project4.locationreminders.data.dto.Result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
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

    private lateinit var db: RemindersDatabase
    lateinit var dao: RemindersDao
    private lateinit var repo: RemindersLocalRepository
    var reminder = ReminderDTO(
        "title",
        "description",
        "LocationTitle",
        30.12345,
        31.23456
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        dao = db.reminderDao()

        repo =RemindersLocalRepository(
            dao,
            Dispatchers.IO
        )
    }

    @After
    fun closeDb() {
        db.close()
    }


//    TO DO: Add testing implementation to the RemindersLocalRepository.kt

    @Test
    fun saveReminder() = runBlocking {
        repo.saveReminder(reminder)

        val result = repo.getReminder(reminder.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertEquals(reminder.title, result.data.title)
        assertEquals(reminder.description, result.data.description)
        assertEquals(reminder.location, result.data.location)
        assertEquals(reminder.latitude, result.data.latitude)
        assertEquals(reminder.longitude, result.data.longitude)
        assertEquals(reminder.id, result.data.id)
    }

    @Test
    fun getReminder_Error() = runBlocking {

        repo.saveReminder(reminder)
        repo.deleteAllReminders()

        val result = repo.getReminder(reminder.id)

        assertEquals(Result.Error("Reminder not found!"), result)
    }

    @Test
    fun removeAllReminders() = runBlockingTest {
        dao.deleteAllReminders()
    }
}