package com.avaupotic.tastynavigator.recyclerview

import android.view.View

interface MyIRecyclerView {
    fun onClick(p0: View?, position: Int) {}
    fun onLongClick(p0: View?, position: Int) {}
}