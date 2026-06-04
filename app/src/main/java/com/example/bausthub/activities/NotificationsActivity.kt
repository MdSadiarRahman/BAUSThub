package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.example.bausthub.adapters.NotificationAdapter
import com.example.bausthub.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: NotificationAdapter
    private var notificationList = mutableListOf<Notification>()

    private lateinit var rvNotifications: RecyclerView
    private lateinit var emptyState: FrameLayout
    private lateinit var btnMarkRead: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)
        btnMarkRead = findViewById(R.id.btnMarkRead)

        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notificationList)
        rvNotifications.adapter = adapter

        loadNotifications()

        btnMarkRead.setOnClickListener {
            markAllAsRead()
        }

        setupNavigation()
    }

    private fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("students").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notificationList.clear()
                    for (doc in snapshot.documents) {
                        val notif = doc.toObject(Notification::class.java)
                        if (notif != null) {
                            notif.id = doc.id
                            notificationList.add(notif)
                        }
                    }
                    adapter.updateData(notificationList)
                    
                    if (notificationList.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        rvNotifications.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        rvNotifications.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        
        db.collection("students").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupNavigation() {
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)

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

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
