package com.example.bausthub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bausthub.R
import com.example.bausthub.models.Post

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvPostCaption: TextView = itemView.findViewById(R.id.tvPostCaption)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvAuthorName.text = post.authorName
        holder.tvPostCaption.text = post.caption
        
        // Load Image with Glide
        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .placeholder(R.drawable.google_button_bg) // Use a subtle placeholder
            .into(holder.ivPostImage)

        // Format Time
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            post.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvPostTime.text = relativeTime
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
