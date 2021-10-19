package com.example.roomstorage.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
class User(
    @ColumnInfo(name ="name")
    var name:String,

    @ColumnInfo(name ="age")
    var age: String,

    @ColumnInfo(name ="gender")
    var gender:String
){
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
}