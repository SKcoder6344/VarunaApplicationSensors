package com.varuna.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        binding.btnUpdateProfile.setOnClickListener { updateProfile() }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.progressBar.visibility = View.GONE
                binding.etName.setText(doc.getString("name") ?: "")
                binding.etEmail.setText(doc.getString("email") ?: "")
                binding.etVillage.setText(doc.getString("village") ?: "")
                binding.tvRole.text = "Role: ${doc.getString("role") ?: "user"}"
                binding.etEmail.isEnabled = false // Email cannot be changed
            }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return
        val name = binding.etName.text.toString().trim()
        val village = binding.etVillage.text.toString().trim()
        if (name.isEmpty()) { binding.etName.error = "Name is required"; return }

        val updates = hashMapOf<String, Any>("name" to name, "village" to village)
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
