package com.example.bausthub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bausthub.R
import com.example.bausthub.models.Comment
import android.text.format.DateUtils

class CommentAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvCommentUser)
        val tvText: TextView = itemView.findViewById(R.id.tvCommentText)
        val tvTime: TextView = itemView.findViewById(R.id.tvCommentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUserName.text = comment.userName
        holder.tvText.text = comment.text
        
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            comment.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
        holder.tvTime.text = relativeTime
    }

    override fun getItemCount(): Int = comments.size
}
