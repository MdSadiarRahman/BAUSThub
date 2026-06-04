package com.example.bausthub.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bausthub.R
import com.example.bausthub.adapters.PostAdapter
import com.example.bausthub.models.Post
import com.google.firebase.firestore.FirebaseFirestore

class PostDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvSinglePost: RecyclerView
    private lateinit var adapter: PostAdapter
    private var postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        db = FirebaseFirestore.getInstance()
        
        val postId = intent.getStringExtra("postId") ?: ""
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        
        rvSinglePost = findViewById(R.id.rvSinglePost)
        rvSinglePost.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(postList)
        rvSinglePost.adapter = adapter
        
        if (postId.isNotEmpty()) {
            loadPost(postId)
        } else {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadPost(postId: String) {
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        post.postId = document.id
                        postList.clear()
                        postList.add(post)
                        adapter.updateData(postList)
                    }
                } else {
                    Toast.makeText(this, "Post no longer exists", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
