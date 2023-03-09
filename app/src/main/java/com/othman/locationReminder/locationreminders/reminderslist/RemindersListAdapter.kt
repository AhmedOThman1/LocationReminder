package com.othman.locationReminder.locationreminders.reminderslist

import com.othman.locationReminder.R
import com.othman.locationReminder.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}