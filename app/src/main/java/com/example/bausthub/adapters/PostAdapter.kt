package com.example.bausthub.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bausthub.R
import com.example.bausthub.models.Post
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvPostCaption: TextView = itemView.findViewById(R.id.tvPostCaptionContainer)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        val btnShare: ImageButton = itemView.findViewById(R.id.btnShare)
        val btnBookmark: ImageButton = itemView.findViewById(R.id.btnBookmark)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        holder.tvAuthorName.text = post.authorName
        
        // Format Caption with Bold Author Name
        val fullCaption = "${post.authorName}  ${post.caption}"
        val spannable = SpannableString(fullCaption)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            post.authorName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.tvPostCaption.text = spannable
        
        // Load Image
        if (post.imageUrl.isNotEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        // Time
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            post.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString().uppercase()
        holder.tvPostTime.text = relativeTime

        // Interactions (Mock for now, can be connected to DB later)
        holder.btnLike.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Liked!", Toast.LENGTH_SHORT).show()
        }

        // More Menu
        holder.btnMore.setOnClickListener {
            val dialog = BottomSheetDialog(holder.itemView.context)
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_post_options, null, false)
            
            val layoutEdit = view.findViewById<LinearLayout>(R.id.layoutEdit)
            val layoutDelete = view.findViewById<LinearLayout>(R.id.layoutDelete)

            // Only show Edit/Delete if it's the user's own post
            if (post.userId != currentUserId) {
                layoutEdit.visibility = View.GONE
                layoutDelete.visibility = View.GONE
            }

            layoutEdit.setOnClickListener {
                dialog.dismiss()
                showEditDialog(holder.itemView.context, post)
            }

            layoutDelete.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirm(holder.itemView.context, post)
            }

            dialog.setContentView(view)
            dialog.show()
        }
    }

    private fun showEditDialog(context: android.content.Context, post: Post) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_caption, null, false)
        
        val etEditCaption = view.findViewById<EditText>(R.id.etEditCaption)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        etEditCaption.setText(post.caption)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newCaption = etEditCaption.text.toString().trim()
            if (newCaption.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                    .update("caption", newCaption)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Caption updated!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDeleteConfirm(context: android.content.Context, post: Post) {
        AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
