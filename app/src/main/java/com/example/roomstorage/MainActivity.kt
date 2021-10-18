package com.example.roomstorage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.roomstorage.database.User
import com.example.roomstorage.database.UserDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_viewholder.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //insert data in database
        submit_button.setOnClickListener {
            details()
        }

        //goto to list of user
        next_page.setOnClickListener {
            userList()
        }
    }
    @InternalCoroutinesApi
    private fun details(){

        val name = input_name.text.toString()
        val age = input_age.text.toString()
        val gender = input_gender.text.toString()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)){
            Toast.makeText(this, Constants.ERROR, Toast.LENGTH_SHORT ).show()
        }
        else{
            GlobalScope.launch {
                UserDatabase.getInstance(this@MainActivity).userDao.insert(User(name,age,gender))
            }
        }
    }

    private fun userList(){

        val intent = Intent(this, DisplayActivity::class.java)
        startActivity(intent)
    }
}