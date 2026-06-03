package com.example.bausthub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bausthub.activities.ProfileActivity
import com.example.bausthub.activities.SearchActivity
import com.example.bausthub.activities.CreatePostActivity
import com.example.bausthub.activities.NotificationsActivity
import com.example.bausthub.adapters.PostAdapter
import com.example.bausthub.models.Post
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvFeed: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var postAdapter: PostAdapter
    private var postList = mutableListOf<Post>()
    private lateinit var tvActiveUsers: android.widget.TextView
    
    private var lastVisible: DocumentSnapshot? = null
    private var isScrolling = false
    private var isLastItemReached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        rvFeed = findViewById(R.id.rvFeed)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        tvActiveUsers = findViewById(R.id.tvActiveUsers)
        val navProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val navSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val navNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)

        rvFeed.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        rvFeed.adapter = postAdapter

        rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                if (isScrolling && (firstVisibleItemPosition + visibleItemCount >= totalItemCount)) {
                    isScrolling = false
                    loadMoreFeed()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }
        })

        swipeRefresh.setOnRefreshListener {
            isLastItemReached = false
            lastVisible = null
            loadFeed()
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(0, 0)
        }

        loadFeed()
        updateActiveUsers()
    }

    private fun updateActiveUsers() {
        db.collection("students").addSnapshotListener { snapshot, _ ->
            val count = snapshot?.size() ?: 0
            tvActiveUsers.text = "● ${count}+ Active"
        }
    }

    private fun loadFeed() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    postList.clear()
                    for (doc in snapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            post.postId = doc.id
                            postList.add(post)
                        }
                    }
                    
                    postAdapter.updateData(postList)
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    
                    if (snapshot.size() < 10) {
                        isLastItemReached = true
                    }
                }
                
                swipeRefresh.isRefreshing = false
                updateEmptyState()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading feed: ${e.message}", Toast.LENGTH_SHORT).show()
                swipeRefresh.isRefreshing = false
            }
    }

    private fun loadMoreFeed() {
        if (isLastItemReached) return

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisible!!)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            post.postId = doc.id
                            postList.add(post)
                        }
                    }
                    
                    postAdapter.updateData(postList)
                    lastVisible = snapshot.documents[snapshot.size() - 1]

                    if (snapshot.size() < 10) {
                        isLastItemReached = true
                    }
                } else {
                    isLastItemReached = true
                }
            }
    }

    private fun updateEmptyState() {
        if (postList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvFeed.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvFeed.visibility = View.VISIBLE
        }
    }
}
