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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bausthub.R
import com.example.bausthub.activities.ImageViewerActivity
import com.example.bausthub.activities.ProfileActivity
import com.example.bausthub.models.Post
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        val ivUserAvatar: ImageView = itemView.findViewById(R.id.ivUserAvatar)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvPostCaption: TextView = itemView.findViewById(R.id.tvPostCaptionContainer)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        val btnShare: ImageButton = itemView.findViewById(R.id.btnShare)
        val btnBookmark: ImageButton = itemView.findViewById(R.id.btnBookmark)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val btnOpenComment: TextView = itemView.findViewById(R.id.btnOpenComment)
        val btnFollow: TextView = itemView.findViewById(R.id.btnFollow)
        
        val layoutDiscussionSpace: LinearLayout = itemView.findViewById(R.id.layoutDiscussionSpace)
        val btnHideDiscussion: TextView = itemView.findViewById(R.id.btnHideDiscussion)
        val rvDiscussionComments: RecyclerView = itemView.findViewById(R.id.rvDiscussionComments)
        val tvDiscussionCommentCount: TextView = itemView.findViewById(R.id.tvDiscussionCommentCount)
        val tvNoCommentsLabel: TextView = itemView.findViewById(R.id.tvNoCommentsLabel)
        val etDiscussionComment: EditText = itemView.findViewById(R.id.etDiscussionComment)
        val btnSendDiscussionComment: ImageButton = itemView.findViewById(R.id.btnSendDiscussionComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()

        holder.tvAuthorName.text = post.authorName
        
        db.collection("students").document(post.userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val profilePicUrl = snapshot.getString("profileImage")
                if (!profilePicUrl.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context).load(profilePicUrl).into(holder.ivUserAvatar)
                }
            }
        }

        val openProfile = View.OnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            intent.putExtra("userId", post.userId)
            holder.itemView.context.startActivity(intent)
        }
        holder.tvAuthorName.setOnClickListener(openProfile)
        holder.ivUserAvatar.setOnClickListener(openProfile)

        val fullCaption = "${post.authorName}  ${post.caption}"
        val spannable = SpannableString(fullCaption)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            post.authorName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.tvPostCaption.text = spannable
        
        if (post.imageUrl.isNotEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.ivPostImage)

            holder.ivPostImage.setOnClickListener {
                val intent = Intent(holder.itemView.context, ImageViewerActivity::class.java)
                intent.putExtra("imageUrl", post.imageUrl)
                intent.putExtra("authorName", post.authorName)
                intent.putExtra("caption", post.caption)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        val relativeTime = DateUtils.getRelativeTimeSpanString(
            post.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString().uppercase()
        holder.tvPostTime.text = relativeTime

        db.collection("posts").document(post.postId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val lCount = snapshot.getLong("likesCount") ?: 0
                val cCount = snapshot.getLong("commentsCount") ?: 0
                holder.tvLikeCount.text = "$lCount BAUSTIANS LIKED"
                holder.tvDiscussionCommentCount.text = "$cCount COMMENTS"
                holder.tvNoCommentsLabel.visibility = if (cCount == 0L) View.VISIBLE else View.GONE
            }
        }

        db.collection("posts").document(post.postId).collection("likes").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    holder.btnLike.setColorFilter(android.graphics.Color.RED)
                } else {
                    holder.btnLike.setColorFilter(android.graphics.Color.GRAY)
                }
            }

        holder.btnLike.setOnClickListener {
            if (post.postId.isEmpty()) return@setOnClickListener
            val likeRef = db.collection("posts").document(post.postId).collection("likes").document(currentUserId)
            likeRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    likeRef.delete()
                    db.collection("posts").document(post.postId).update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
                } else {
                    likeRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    db.collection("posts").document(post.postId).update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                }
            }
        }

        holder.btnShare.setOnClickListener {
            val shareText = "${post.authorName}: ${post.caption}\n\nShared from BAUSThub"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            holder.itemView.context.startActivity(Intent.createChooser(intent, "Share post via"))
        }

        db.collection("students").document(currentUserId).collection("savedPosts").document(post.postId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    holder.btnBookmark.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
                } else {
                    holder.btnBookmark.setColorFilter(android.graphics.Color.GRAY)
                }
            }

        holder.btnBookmark.setOnClickListener {
            if (post.postId.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Error: Post ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val savedRef = db.collection("students").document(currentUserId).collection("savedPosts").document(post.postId)
            savedRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    savedRef.delete().addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Removed from Vault", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    savedRef.set(post).addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Saved to Vault", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val commentsList = mutableListOf<com.example.bausthub.models.Comment>()
        val commentAdapter = CommentAdapter(commentsList)
        holder.rvDiscussionComments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.itemView.context)
        holder.rvDiscussionComments.adapter = commentAdapter

        fun toggleDiscussion(show: Boolean) {
            if (show) {
                holder.layoutDiscussionSpace.visibility = View.VISIBLE
                holder.btnOpenComment.visibility = View.GONE
                db.collection("posts").document(post.postId).collection("comments")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            commentsList.clear()
                            for (doc in snapshot.documents) {
                                val comment = doc.toObject(com.example.bausthub.models.Comment::class.java)
                                if (comment != null) commentsList.add(comment)
                            }
                            commentAdapter.notifyDataSetChanged()
                            holder.tvDiscussionCommentCount.text = "${commentsList.size} COMMENTS"
                            holder.tvNoCommentsLabel.visibility = if (commentsList.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
            } else {
                holder.layoutDiscussionSpace.visibility = View.GONE
                holder.btnOpenComment.visibility = View.VISIBLE
            }
        }

        holder.btnComment.setOnClickListener { toggleDiscussion(true) }
        holder.btnOpenComment.setOnClickListener { toggleDiscussion(true) }
        holder.btnHideDiscussion.setOnClickListener { toggleDiscussion(false) }

        holder.btnSendDiscussionComment.setOnClickListener {
            val text = holder.etDiscussionComment.text.toString().trim()
            if (text.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                val comment = hashMapOf(
                    "userId" to currentUserId,
                    "userName" to (user?.displayName ?: "BAUSTian"),
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("posts").document(post.postId).collection("comments")
                    .add(comment)
                    .addOnSuccessListener {
                        holder.etDiscussionComment.text.clear()
                        db.collection("posts").document(post.postId).update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                    }
            }
        }

        holder.btnMore.setOnClickListener {
            val dialog = BottomSheetDialog(holder.itemView.context)
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_post_options, null, false)
            
            val layoutEdit = view.findViewById<LinearLayout>(R.id.layoutEdit)
            val layoutDelete = view.findViewById<LinearLayout>(R.id.layoutDelete)

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
                val uid = post.userId
                FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                    .delete()
                    .addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("students").document(uid)
                            .update("postsCount", com.google.firebase.firestore.FieldValue.increment(-1))

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
