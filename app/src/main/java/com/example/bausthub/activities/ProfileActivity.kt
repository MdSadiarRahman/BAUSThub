package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    private lateinit var ivProfilePic: ImageView
    
    private lateinit var tvPostCount: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileBio) 
        rvProfileFeed = findViewById(R.id.rvProfileFeed)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        
        tvPostCount = findViewById(R.id.tvPostCount)
        tvFollowersCount = findViewById(R.id.tvFollowersCount)
        tvFollowingCount = findViewById(R.id.tvFollowingCount)

        // Setup RecyclerView
        rvProfileFeed.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        rvProfileFeed.adapter = postAdapter
        
        val btnProfileMenu = findViewById<LinearLayout>(R.id.btnProfileMenu)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnMyPosts = findViewById<LinearLayout>(R.id.btnMyPosts)
        val btnVault = findViewById<LinearLayout>(R.id.btnVault)
        
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

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
            showProfileMenu(view)
        }

        loadUserData()
        syncCounts() // One-time sync to fix any negative or incorrect counts
        loadUserPosts() // Default view
    }

    private fun syncCounts() {
        val uid = auth.currentUser?.uid ?: return
        
        // Sync Posts Count
        db.collection("posts").whereEqualTo("userId", uid).count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                db.collection("students").document(uid).update("postsCount", snapshot.count)
            }
            
        // Sync Followers Count
        db.collection("students").document(uid).collection("followers").count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                db.collection("students").document(uid).update("followersCount", snapshot.count)
            }
            
        // Sync Following Count
        db.collection("students").document(uid).collection("following").count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                db.collection("students").document(uid).update("followingCount", snapshot.count)
            }
    }

    private fun showProfileMenu(anchorView: View) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_profile_menu, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set listeners for menu items
        popupView.findViewById<LinearLayout>(R.id.menuSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            popupWindow.dismiss()
        }

        popupView.findViewById<LinearLayout>(R.id.menuSignOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            popupWindow.dismiss()
        }

        // Show the popup menu
        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(anchorView, 0, 10) 
        
        // Adjust position to the left so it aligns with the button's right edge
        popupView.post {
            popupWindow.update(anchorView, -(popupView.width - anchorView.width), 10, -1, -1)
        }
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

        db.collection("students").document(uid).addSnapshotListener { document, _ ->
            if (document != null && document.exists()) {
                tvName.text = document.getString("name")
                val bio = document.getString("bio") ?: "BAUSTian Hubber"
                tvEmail.text = bio

                // Dynamic Counts with Negative Protection
                val postCount = document.getLong("postsCount") ?: 0
                val followersCount = document.getLong("followersCount") ?: 0
                val followingCount = document.getLong("followingCount") ?: 0

                tvPostCount.text = Math.max(0, postCount).toString()
                tvFollowersCount.text = Math.max(0, followersCount).toString()
                tvFollowingCount.text = Math.max(0, followingCount).toString()

                val profilePicUrl = document.getString("profileImage")
                if (!profilePicUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profilePicUrl).into(ivProfilePic)
                }
            }
        }
    }
}
