package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.example.bausthub.adapters.UserAdapter
import com.example.bausthub.models.User
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private var userList = mutableListOf<User>()
    
    private lateinit var etSearch: EditText
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var initialState: FrameLayout

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        db = FirebaseFirestore.getInstance()

        etSearch = findViewById(R.id.etSearch)
        rvSearchResults = findViewById(R.id.rvSearchResults)
        initialState = findViewById(R.id.initialState)

        rvSearchResults.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList)
        rvSearchResults.adapter = userAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                
                // Debounce search to avoid excessive Firestore queries
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { searchUsers(query) }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupNavigation()
    }

    private fun searchUsers(query: String) {
        if (query.isEmpty()) {
            userList.clear()
            userAdapter.updateData(userList)
            rvSearchResults.visibility = View.GONE
            initialState.visibility = View.VISIBLE
            return
        }

        // Search for names starting with the query (Case-sensitive in Firestore)
        val capitalizedQuery = query.replaceFirstChar { it.uppercase() }

        db.collection("students")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val results = mutableListOf<User>()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        user.uid = doc.id
                        results.add(user)
                    }
                }
                
                // If no results for exact query, try with first letter capitalized
                if (results.isEmpty() && capitalizedQuery != query) {
                    db.collection("students")
                        .orderBy("name")
                        .startAt(capitalizedQuery)
                        .endAt(capitalizedQuery + "\uf8ff")
                        .get()
                        .addOnSuccessListener { capSnapshot ->
                            for (doc in capSnapshot.documents) {
                                val user = doc.toObject(User::class.java)
                                if (user != null) {
                                    user.uid = doc.id
                                    results.add(user)
                                }
                            }
                            updateUI(results)
                        }
                } else {
                    updateUI(results)
                }
            }
    }

    private fun updateUI(results: List<User>) {
        userList.clear()
        userList.addAll(results)
        userAdapter.updateData(userList)
        
        if (userList.isEmpty() && etSearch.text.toString().isNotEmpty()) {
            // Optional: Show "No users found" state
            rvSearchResults.visibility = View.GONE
            initialState.visibility = View.VISIBLE
        } else if (userList.isEmpty()) {
            rvSearchResults.visibility = View.GONE
            initialState.visibility = View.VISIBLE
        } else {
            rvSearchResults.visibility = View.VISIBLE
            initialState.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
    }
}
