package com.example.bausthub.adapters

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bausthub.R
import com.example.bausthub.models.Notification
import com.google.firebase.firestore.FirebaseFirestore

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivNotifUser: ImageView = itemView.findViewById(R.id.ivNotifUser)
        val tvNotifMessage: TextView = itemView.findViewById(R.id.tvNotifMessage)
        val tvNotifTime: TextView = itemView.findViewById(R.id.tvNotifTime)
        val unreadDot: View = itemView.findViewById(R.id.unreadDot)
        val layout: View = itemView.findViewById(R.id.notificationLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notif = notifications[position]

        val fullText = "${notif.fromName} ${notif.message}"
        val spannable = SpannableString(fullText)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            notif.fromName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.tvNotifMessage.text = spannable

        val timeAgo = DateUtils.getRelativeTimeSpanString(
            notif.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString().uppercase()
        holder.tvNotifTime.text = timeAgo

        // Fetch user profile image from Firestore
        FirebaseFirestore.getInstance().collection("students").document(notif.fromId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val profilePicUrl = snapshot.getString("profileImage")
                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(holder.itemView.context).load(profilePicUrl).into(holder.ivNotifUser)
                    } else {
                        holder.ivNotifUser.setImageResource(R.mipmap.ic_launcher)
                    }
                }
            }

        if (notif.isRead) {
            holder.unreadDot.visibility = View.GONE
            holder.layout.setBackgroundColor(android.graphics.Color.WHITE)
        } else {
            holder.unreadDot.visibility = View.VISIBLE
            holder.layout.setBackgroundColor(android.graphics.Color.parseColor("#F1F5F9"))
        }

        holder.itemView.setOnClickListener {
            if (!notif.isRead) {
                FirebaseFirestore.getInstance().collection("students")
                    .document(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "")
                    .collection("notifications").document(notif.id)
                    .update("isRead", true)
            }
            
            val context = holder.itemView.context
            when (notif.type) {
                "post", "like" -> {
                    if (notif.postId.isNotEmpty()) {
                        val intent = Intent(context, com.example.bausthub.activities.PostDetailActivity::class.java)
                        intent.putExtra("postId", notif.postId)
                        context.startActivity(intent)
                    }
                }
                "follow" -> {
                    val intent = Intent(context, com.example.bausthub.activities.ProfileActivity::class.java)
                    intent.putExtra("userId", notif.fromId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newList: List<Notification>) {
        notifications = newList
        notifyDataSetChanged()
    }
}
