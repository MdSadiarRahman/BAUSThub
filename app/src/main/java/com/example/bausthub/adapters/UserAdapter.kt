package com.example.bausthub.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bausthub.R
import com.example.bausthub.activities.ProfileActivity
import com.example.bausthub.models.User
import com.google.android.material.imageview.ShapeableImageView

class UserAdapter(private var users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ShapeableImageView = itemView.findViewById(R.id.ivUserImage)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserBio: TextView = itemView.findViewById(R.id.tvUserBio)
        val tvUserDept: TextView = itemView.findViewById(R.id.tvUserDept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserName.text = user.name
        holder.tvUserBio.text = user.bio
        holder.tvUserDept.text = holder.itemView.context.getString(R.string.user_dept_batch, user.department, user.batch)

        if (user.profileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(user.profileImage).into(holder.ivUserImage)
        } else {
            holder.ivUserImage.setImageResource(R.mipmap.ic_launcher)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            intent.putExtra("userId", user.uid)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
