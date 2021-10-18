package com.example.roomstorage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.roomstorage.R
import com.example.roomstorage.database.User
import kotlinx.android.synthetic.main.user_viewholder.view.*

class UserAdapter(val list: List<User>):RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_viewholder, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val data = list[position]
        holder.itemView.text_name.text = data.name
        holder.itemView.text_age.text = data.age
        holder.itemView.text_gender.text = data.gender
    }

    override fun getItemCount(): Int {
        return list.size
    }
}