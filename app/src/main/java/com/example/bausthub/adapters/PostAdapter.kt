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

            // Image Click to View Full Screen
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

        // Time
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            post.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString().uppercase()
        holder.tvPostTime.text = relativeTime

        // --- Interaction Logic ---

        // Listen for Real-time Post Data (Likes/Comments Count)
        db.collection("posts").document(post.postId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val lCount = snapshot.getLong("likesCount") ?: 0
                val cCount = snapshot.getLong("commentsCount") ?: 0
                holder.tvLikeCount.text = "$lCount BAUSTIANS LIKED"
                holder.tvDiscussionCommentCount.text = "$cCount COMMENTS"
                holder.tvNoCommentsLabel.visibility = if (cCount == 0L) View.VISIBLE else View.GONE
            }
        }

        // Like Status Check
        db.collection("posts").document(post.postId).collection("likes").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    holder.btnLike.setColorFilter(android.graphics.Color.RED)
                } else {
                    holder.btnLike.setColorFilter(android.graphics.Color.GRAY)
                }
            }

        // Like Click
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

        // Share Click
        holder.btnShare.setOnClickListener {
            val shareText = "${post.authorName}: ${post.caption}\n\nShared from BAUSThub"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            holder.itemView.context.startActivity(Intent.createChooser(intent, "Share post via"))
        }

        // Bookmark Click (Vault)
        db.collection("students").document(currentUserId).collection("savedPosts").document(post.postId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    holder.btnBookmark.setColorFilter(android.graphics.Color.parseColor("#FFD700")) // Yellow
                } else {
                    holder.btnBookmark.setColorFilter(android.graphics.Color.GRAY)
                }
            }

        holder.btnBookmark.setOnClickListener {
            val savedRef = db.collection("students").document(currentUserId).collection("savedPosts").document(post.postId)
            savedRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    savedRef.delete()
                    Toast.makeText(holder.itemView.context, "Removed from Vault", Toast.LENGTH_SHORT).show()
                } else {
                    savedRef.set(post)
                    Toast.makeText(holder.itemView.context, "Saved to Vault", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Comment Discussion Logic
        val commentsList = mutableListOf<com.example.bausthub.models.Comment>()
        val commentAdapter = CommentAdapter(commentsList)
        holder.rvDiscussionComments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.itemView.context)
        holder.rvDiscussionComments.adapter = commentAdapter

        fun toggleDiscussion(show: Boolean) {
            if (show) {
                holder.layoutDiscussionSpace.visibility = View.VISIBLE
                holder.btnOpenComment.visibility = View.GONE
                // Load Comments
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

        // Follow Logic
        if (post.userId == currentUserId) {
            holder.btnFollow.visibility = View.GONE
        } else {
            holder.btnFollow.visibility = View.VISIBLE
            db.collection("students").document(currentUserId).collection("following").document(post.userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        holder.btnFollow.text = " • Following"
                        holder.btnFollow.setTextColor(android.graphics.Color.GRAY)
                    } else {
                        holder.btnFollow.text = " • Follow"
                        holder.btnFollow.setTextColor(android.graphics.Color.parseColor("#10B981"))
                    }
                }

            holder.btnFollow.setOnClickListener {
                val followingRef = db.collection("students").document(currentUserId).collection("following").document(post.userId)
                val followersRef = db.collection("students").document(post.userId).collection("followers").document(currentUserId)

                followingRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        followingRef.delete()
                        followersRef.delete()
                        db.collection("students").document(currentUserId).update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1))
                        db.collection("students").document(post.userId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    } else {
                        followingRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                        followersRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                        db.collection("students").document(currentUserId).update("followingCount", com.google.firebase.firestore.FieldValue.increment(1))
                        db.collection("students").document(post.userId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(1))
                    }
                }
            }
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

    private fun showCommentsDialog(context: android.content.Context, post: Post) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_comments, null, false)
        
        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSendComment)
        val rvComments = view.findViewById<RecyclerView>(R.id.rvComments)
        
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        
        val commentsList = mutableListOf<com.example.bausthub.models.Comment>()
        val adapter = CommentAdapter(commentsList)
        rvComments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rvComments.adapter = adapter

        // Fetch Comments
        db.collection("posts").document(post.postId).collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    commentsList.clear()
                    for (doc in snapshot.documents) {
                        val comment = doc.toObject(com.example.bausthub.models.Comment::class.java)
                        if (comment != null) commentsList.add(comment)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        btnSend.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                btnSend.isEnabled = false // Prevent multiple clicks
                
                val user = FirebaseAuth.getInstance().currentUser
                val userName = user?.displayName ?: "BAUSTian"
                
                val comment = hashMapOf(
                    "userId" to currentUserId,
                    "userName" to userName,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                
                db.collection("posts").document(post.postId).collection("comments")
                    .add(comment)
                    .addOnSuccessListener {
                        etComment.text.clear()
                        db.collection("posts").document(post.postId).update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                        btnSend.isEnabled = true
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
                        btnSend.isEnabled = true
                    }
            }
        }

        dialog.setContentView(view)
        dialog.show()
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
                        // Update Post Count
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
