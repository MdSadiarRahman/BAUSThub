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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private var postList = mutableListOf<Post>()
    private var profileListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var dataListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var followListener: com.google.firebase.firestore.ListenerRegistration? = null

    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvEmail: TextView
    private lateinit var rvProfileFeed: RecyclerView
    private lateinit var ivProfilePic: ImageView
    private lateinit var ivCoverPhoto: ImageView
    
    private lateinit var tvPostCount: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView

    private lateinit var btnBack: ImageButton
    private lateinit var btnFollowProfile: Button
    private lateinit var btnProfileMenu: LinearLayout
    private lateinit var btnVault: LinearLayout
    private lateinit var btnAdd: ImageButton

    private var targetUid: String? = null
    private var isOwnProfile = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        targetUid = intent.getStringExtra("userId")
        val currentUid = auth.currentUser?.uid ?: ""
        isOwnProfile = targetUid == null || targetUid == currentUid
        if (targetUid == null) targetUid = currentUid

        tvName = findViewById(R.id.tvProfileName)
        tvBio = findViewById(R.id.tvProfileBio)
        tvEmail = findViewById(R.id.tvProfileEmail)
        rvProfileFeed = findViewById(R.id.rvProfileFeed)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        ivCoverPhoto = findViewById(R.id.coverPhoto)
        
        tvPostCount = findViewById(R.id.tvPostCount)
        tvFollowersCount = findViewById(R.id.tvFollowersCount)
        tvFollowingCount = findViewById(R.id.tvFollowingCount)

        btnBack = findViewById(R.id.btnBack)
        btnFollowProfile = findViewById(R.id.btnFollowProfile)
        btnProfileMenu = findViewById(R.id.btnProfileMenu)
        btnVault = findViewById(R.id.btnVault)
        btnAdd = findViewById(R.id.btnAdd)

        rvProfileFeed.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        rvProfileFeed.adapter = postAdapter
        
        setupUI()
        setupListeners()
        loadUserPosts()
    }

    private fun setupUI() {
        if (isOwnProfile) {
            btnBack.visibility = View.GONE
            btnFollowProfile.visibility = View.GONE
            btnProfileMenu.visibility = View.VISIBLE
            btnVault.visibility = View.VISIBLE
            btnAdd.visibility = View.VISIBLE
        } else {
            btnBack.visibility = View.VISIBLE
            btnFollowProfile.visibility = View.VISIBLE
            btnProfileMenu.visibility = View.GONE
            btnVault.visibility = View.GONE
            btnAdd.visibility = View.GONE
            
            btnBack.setOnClickListener { finish() }
            
            btnFollowProfile.setOnClickListener {
                toggleFollow()
            }
        }

        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnMyPosts = findViewById<LinearLayout>(R.id.btnMyPosts)

        btnMyPosts.setOnClickListener {
            updateTabUI(true)
            loadUserPosts()
        }

        btnVault.setOnClickListener {
            updateTabUI(false)
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

        updateTabUI(true)
    }

    private fun setupListeners() {
        val uid = targetUid ?: return
        val currentUid = auth.currentUser?.uid ?: ""

        dataListener = db.collection("students").document(uid).addSnapshotListener { document, _ ->
            if (document != null && document.exists()) {
                tvName.text = document.getString("name")
                tvEmail.text = document.getString("email") ?: "student@baust.edu.bd"
                tvBio.text = document.getString("bio") ?: "BAUSTian Hubber"

                tvPostCount.text = (document.getLong("postsCount") ?: 0).toString()
                tvFollowersCount.text = (document.getLong("followersCount") ?: 0).toString()
                tvFollowingCount.text = (document.getLong("followingCount") ?: 0).toString()

                val profilePicUrl = document.getString("profileImage")
                if (!profilePicUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profilePicUrl).into(ivProfilePic)
                }
                
                val coverUrl = document.getString("coverImage")
                if (!coverUrl.isNullOrEmpty()) {
                    Glide.with(this).load(coverUrl).into(ivCoverPhoto)
                }
            }
        }

        if (!isOwnProfile) {
            followListener = db.collection("students").document(uid).collection("followers").document(currentUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        btnFollowProfile.text = "Following"
                        btnFollowProfile.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY))
                        btnFollowProfile.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.checkbox_on_background, 0, 0, 0)
                    } else {
                        btnFollowProfile.text = "Follow Hubber"
                        btnFollowProfile.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981")))
                        btnFollowProfile.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_input_add, 0, 0, 0)
                    }
                }
        }
    }

    private fun toggleFollow() {
        val uid = targetUid ?: return
        val currentUid = auth.currentUser?.uid ?: return
        
        val followersRef = db.collection("students").document(uid).collection("followers").document(currentUid)
        val followingRef = db.collection("students").document(currentUid).collection("following").document(uid)
        
        followersRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                followersRef.delete()
                followingRef.delete()
                db.collection("students").document(uid).update("followersCount", FieldValue.increment(-1))
                db.collection("students").document(currentUid).update("followingCount", FieldValue.increment(-1))
            } else {
                followersRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                followingRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                db.collection("students").document(uid).update("followersCount", FieldValue.increment(1))
                db.collection("students").document(currentUid).update("followingCount", FieldValue.increment(1))
                
                // Add Notification
                val notification = hashMapOf(
                    "type" to "follow",
                    "fromId" to currentUid,
                    "fromName" to (auth.currentUser?.displayName ?: "Someone"),
                    "timestamp" to System.currentTimeMillis(),
                    "message" to "started following you."
                )
                db.collection("students").document(uid).collection("notifications").add(notification)
            }
        }
    }

    private fun updateTabUI(isMyPosts: Boolean) {
        val btnMyPosts = findViewById<LinearLayout>(R.id.btnMyPosts)
        val ivMyPosts = btnMyPosts.getChildAt(0) as ImageView
        val tvMyPosts = btnMyPosts.getChildAt(1) as TextView
        
        val ivVault = btnVault.getChildAt(0) as ImageView
        val tvVault = btnVault.getChildAt(1) as TextView

        if (isMyPosts) {
            btnMyPosts.setBackgroundResource(R.drawable.bg_pill_active)
            tvMyPosts.setTextColor(android.graphics.Color.WHITE)
            ivMyPosts.setColorFilter(android.graphics.Color.WHITE)
            
            btnVault.setBackgroundResource(0)
            tvVault.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
            ivVault.setColorFilter(android.graphics.Color.parseColor("#94A3B8"))
        } else {
            btnMyPosts.setBackgroundResource(0)
            tvMyPosts.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
            ivMyPosts.setColorFilter(android.graphics.Color.parseColor("#94A3B8"))
            
            btnVault.setBackgroundResource(R.drawable.bg_pill_active)
            tvVault.setTextColor(android.graphics.Color.WHITE)
            ivVault.setColorFilter(android.graphics.Color.WHITE)
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

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(anchorView, 0, 10) 
        
        popupView.post {
            popupWindow.update(anchorView, -(popupView.width - anchorView.width), 10, -1, -1)
        }
    }

    private fun loadUserPosts() {
        val uid = targetUid ?: return
        profileListener?.remove()
        profileListener = db.collection("posts")
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
        if (!isOwnProfile) return
        val uid = auth.currentUser?.uid ?: return
        profileListener?.remove()
        profileListener = db.collection("students").document(uid).collection("savedPosts")
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

    override fun onDestroy() {
        super.onDestroy()
        profileListener?.remove()
        dataListener?.remove()
        followListener?.remove()
    }
}
