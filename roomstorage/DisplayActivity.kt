package com.example.roomstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomstorage.adapter.UserAdapter
import com.example.roomstorage.database.UserDatabase
import kotlinx.android.synthetic.main.activity_display.*
import kotlinx.android.synthetic.main.user_viewholder.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

class DisplayActivity : AppCompatActivity() {

    private lateinit var adapter:UserAdapter

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        userList()
    }

    @InternalCoroutinesApi
    private fun userList(){

        GlobalScope.launch {
           val list  = UserDatabase.getInstance(this@DisplayActivity).userDao.getAll()
            val adapter = UserAdapter(list)
            recyclerView.adapter = adapter
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}