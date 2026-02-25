package com.varuna.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.R
import com.varuna.app.databinding.ActivityMainBinding
import com.varuna.app.fragments.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userRole: String = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)
        setupDrawer()
        setupBottomNavigation()
        fetchUserRole()

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            binding.toolbar.title = "Dashboard"
        }
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    binding.toolbar.title = "Dashboard"
                    true
                }
                R.id.nav_water_quality -> {
                    loadFragment(WaterQualityFragment())
                    binding.toolbar.title = "Water Quality"
                    true
                }
                R.id.nav_disease_risk -> {
                    loadFragment(DiseaseRiskFragment())
                    binding.toolbar.title = "Disease Risk"
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    binding.toolbar.title = "Reports"
                    true
                }
                R.id.nav_alerts -> {
                    loadFragment(AlertsFragment())
                    binding.toolbar.title = "Alerts"
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role") ?: "user"
                val navHeader = binding.navView.getHeaderView(0)
                navHeader.findViewById<TextView>(R.id.tv_user_name)?.text = doc.getString("name") ?: "User"
                navHeader.findViewById<TextView>(R.id.tv_user_email)?.text = doc.getString("email") ?: ""
                navHeader.findViewById<TextView>(R.id.tv_user_village)?.text = doc.getString("village") ?: ""

                // Show admin menu if role is admin
                binding.navView.menu.findItem(R.id.nav_admin)?.isVisible = (userRole == "admin")
            }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_education -> {
                startActivity(Intent(this, EducationActivity::class.java))
            }
            R.id.nav_chatbot -> {
                startActivity(Intent(this, ChatbotActivity::class.java))
            }
            R.id.nav_alerts_drawer -> {
                loadFragment(AlertsFragment())
                binding.toolbar.title = "Alerts"
            }
            R.id.nav_help -> {
                loadFragment(HelpFragment())
                binding.toolbar.title = "Help & Requests"
            }
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())
                binding.toolbar.title = "My Profile"
            }
            R.id.nav_admin -> {
                loadFragment(AdminFragment())
                binding.toolbar.title = "Admin Panel"
            }
            R.id.nav_logout -> {
                performLogout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
