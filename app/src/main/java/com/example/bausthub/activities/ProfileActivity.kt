package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.example.bausthub.adapters.PostAdapter
import com.example.bausthub.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private var postList = mutableListOf<Post>()

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var rvProfileFeed: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileBio) 
        rvProfileFeed = findViewById(R.id.rvProfileFeed)

        // Setup RecyclerView
        rvProfileFeed.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        rvProfileFeed.adapter = postAdapter
        
        val btnProfileMenu = findViewById<ImageButton>(R.id.btnProfileMenu)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        
        val btnMyPosts = findViewById<LinearLayout>(R.id.btnMyPosts)
        val btnVault = findViewById<LinearLayout>(R.id.btnVault)

        btnMyPosts.setOnClickListener {
            loadUserPosts()
        }

        btnVault.setOnClickListener {
            loadSavedPosts()
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnProfileMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> {
                        Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_sign_out -> {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        loadUserData()
        loadUserPosts() // Default view
    }

    private fun loadUserPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("posts")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    postList.clear()
                    for (doc in snapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            post.postId = doc.id
                            postList.add(post)
                        }
                    }
                    postAdapter.updateData(postList)
                }
            }
    }

    private fun loadSavedPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("students").document(uid).collection("savedPosts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    postList.clear()
                    for (doc in snapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            post.postId = doc.id
                            postList.add(post)
                        }
                    }
                    postAdapter.updateData(postList)
                }
            }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("students").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    tvName.text = document.getString("name")
                    val studentId = document.getString("studentId")
                    val dept = document.getString("department")
                    val batch = document.getString("batch")
                    val bio = document.getString("bio") ?: "BAUSTian Hubber"
                    
                    tvEmail.text = bio
                }
            }
    }
}
