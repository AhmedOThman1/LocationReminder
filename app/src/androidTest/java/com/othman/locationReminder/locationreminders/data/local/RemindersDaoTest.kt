package com.othman.locationReminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.othman.locationReminder.locationreminders.data.dto.ReminderDTO
import com.othman.locationReminder.locationreminders.data.local.RemindersDao
import com.othman.locationReminder.locationreminders.data.local.RemindersDatabase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TO DO: Add testing implementation to the RemindersDao.kt
    lateinit var db: RemindersDatabase
    lateinit var dao: RemindersDao
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

    }

    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun getAllReminders() = runBlockingTest {
        dao.saveReminder(reminder)
        dao.saveReminder(reminder)
        dao.saveReminder(reminder)
        dao.saveReminder(reminder)

        val reminders = dao.getReminders()
        assertNotNull(reminders)
    }

    @Test
    fun getReminderById() = runBlockingTest {
        dao.saveReminder(reminder)

        val savedReminderDto = dao.getReminderById(reminder.id)
        assertNotNull(savedReminderDto)
        assertEquals(reminder.title, savedReminderDto?.title)
        assertEquals(reminder.description, savedReminderDto?.description)
        assertEquals(reminder.location, savedReminderDto?.location)
        assertEquals(reminder.latitude, savedReminderDto?.latitude)
        assertEquals(reminder.longitude, savedReminderDto?.longitude)
        assertEquals(reminder.id, savedReminderDto?.id)
    }

    @Test
    fun saveReminder() = runBlockingTest {
        dao.saveReminder(reminder)

        val savedReminderDto = dao.getReminderById(reminder.id)

        assertNotNull(savedReminderDto)
        assertEquals(reminder, savedReminderDto)
    }

    @Test
    fun removeAllReminders() = runBlockingTest {
        dao.deleteAllReminders()
    }
}