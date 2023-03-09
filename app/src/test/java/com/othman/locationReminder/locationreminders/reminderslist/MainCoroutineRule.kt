package com.othman.locationReminder.locationreminders.reminderslist


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(val coroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
    TestWatcher(), TestCoroutineScope by TestCoroutineScope(coroutineDispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(coroutineDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}