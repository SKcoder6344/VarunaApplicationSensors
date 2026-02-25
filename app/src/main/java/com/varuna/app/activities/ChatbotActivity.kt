package com.varuna.app.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuna.app.adapters.ChatAdapter
import com.varuna.app.databinding.ActivityChatbotBinding
import com.varuna.app.model.ChatMessage

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Varuna AI Advisor"

        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // Welcome message
        addBotMessage("👋 Hello! I'm Varuna AI Advisor.\n\nI can help you with:\n• Water quality guidelines (WHO/BIS)\n• Disease prevention advice\n• Purification methods\n• Interpreting your WQI results\n\nHow can I assist you today?")

        // Quick action chips
        setupQuickActions()

        binding.btnSend.setOnClickListener {
            val userMsg = binding.etMessage.text.toString().trim()
            if (userMsg.isNotEmpty()) {
                sendMessage(userMsg)
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun setupQuickActions() {
        binding.chipWqiHelp.setOnClickListener { sendMessage("What is WQI and how is it calculated?") }
        binding.chipPurification.setOnClickListener { sendMessage("What are the best water purification methods?") }
        binding.chipDiseaseRisk.setOnClickListener { sendMessage("How to prevent cholera and typhoid?") }
        binding.chipWhoStandards.setOnClickListener { sendMessage("What are WHO water quality standards?") }
    }

    private fun sendMessage(text: String) {
        addUserMessage(text)
        binding.btnSend.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // Simulate AI response (integrate with actual AI/NLP API here)
        android.os.Handler(mainLooper).postDelayed({
            val response = getAIResponse(text.lowercase())
            addBotMessage(response)
            binding.btnSend.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }, 800)
    }

    private fun getAIResponse(query: String): String {
        return when {
            query.contains("wqi") || query.contains("water quality index") ->
                """📊 **Water Quality Index (WQI)**
                
WQI is a score from 0-100 that summarizes overall water quality:
• 75-100: ✅ SAFE – Good for drinking
• 50-74: ⚠️ MODERATE – Treat before use
• 0-49: 🚫 UNSAFE – Do NOT drink directly

The Varuna app calculates WQI using:
pH, TDS, Turbidity, Hardness, Temperature, Chloride, and Dissolved Oxygen

These are compared against WHO/BIS standards using weighted scoring."""

            query.contains("purif") || query.contains("filter") || query.contains("treat") ->
                """🧪 **Water Purification Methods**

1. **Boiling** – Most effective for biological contaminants. Boil for 10+ min.
2. **RO (Reverse Osmosis)** – Removes TDS, heavy metals, bacteria
3. **UV Disinfection** – Kills bacteria and viruses
4. **Chlorination** – Add 0.2 mg/L chlorine; kills pathogens
5. **Sand Filtration** – Reduces turbidity
6. **Distillation** – Removes all dissolved solids

For specific issues:
• High TDS → RO or Distillation
• High Turbidity → Filtration + Sedimentation
• Low pH → Lime Treatment
• Microbial Risk → Boiling + Chlorination"""

            query.contains("cholera") || query.contains("typhoid") || query.contains("disease") || query.contains("prevent") ->
                """🦠 **Disease Prevention Guidelines**

**Cholera Prevention:**
• Drink only boiled or treated water
• Wash hands with soap after toilet use
• Avoid raw/undercooked seafood
• Disinfect water storage containers

**Typhoid Prevention:**
• Avoid drinking untreated water
• Use bottled water when in doubt
• Maintain strict hand hygiene
• Get vaccinated if at risk

**Diarrhea Prevention:**
• ORS (Oral Rehydration Solution) if symptoms occur
• Boil drinking water
• Eat freshly cooked food
• Keep food covered

🏥 Seek medical attention if symptoms persist!"""

            query.contains("who") || query.contains("standard") || query.contains("limit") || query.contains("bis") ->
                """📋 **WHO & BIS Water Quality Standards**

| Parameter | WHO Limit | BIS Limit | Unit |
|-----------|-----------|-----------|------|
| pH | 6.5–8.5 | 6.5–8.5 | – |
| TDS | ≤500 | ≤500 | mg/L |
| Turbidity | ≤4 | ≤5 | NTU |
| Hardness | ≤200 | ≤300 | mg/L |
| Chloride | ≤250 | ≤250 | mg/L |
| DO | ≥5 | ≥5 | mg/L |
| Fluoride | ≤1.5 | ≤1.0 | mg/L |
| Nitrate | ≤50 | ≤45 | mg/L |

BIS = Bureau of Indian Standards"""

            query.contains("ph") ->
                """🔬 **pH in Water Quality**

pH measures acidity/alkalinity (0-14 scale):
• < 7 = Acidic
• 7 = Neutral
• > 7 = Alkaline

**Safe range: 6.5 – 8.5**

Low pH (acidic):
• Corrosive to pipes
• Metallic taste
• Treatment: Lime or soda ash

High pH (alkaline):
• Bitter taste
• Scaling in pipes
• Treatment: CO₂ injection or acid neutralization"""

            query.contains("tds") ->
                """💧 **TDS (Total Dissolved Solids)**

TDS measures total minerals dissolved in water.

**WHO/BIS Safe Limit: ≤500 mg/L**

TDS Ranges:
• < 300: Excellent
• 300–600: Good
• 600–900: Fair
• 900–1200: Poor
• > 1200: Unacceptable

High TDS Treatment:
→ Reverse Osmosis (RO)
→ Distillation
→ Ion Exchange"""

            query.contains("turbid") || query.contains("cloudy") ->
                """🌫️ **Turbidity in Water**

Turbidity measures water cloudiness (NTU units).

**WHO Limit: ≤4 NTU | BIS: ≤5 NTU**

Causes:
• Suspended soil particles
• Algae
• Bacteria colonies
• Organic matter

Treatment:
1. Coagulation (Alum addition)
2. Flocculation
3. Sedimentation (24 hours)
4. Sand/Membrane Filtration
5. Disinfection"""

            query.contains("hello") || query.contains("hi") || query.contains("hey") ->
                "Hello! 👋 How can I help you with water quality today? You can ask about WQI, purification methods, disease prevention, or WHO standards."

            query.contains("thank") ->
                "You're welcome! 😊 Stay safe and drink clean water. Don't hesitate to ask if you need more help!"

            else ->
                """I'm not sure about that specific query. Here are topics I can help with:

🔹 Type "WQI" – Learn about Water Quality Index
🔹 Type "purification" – Water treatment methods
🔹 Type "disease" or "cholera" – Disease prevention
🔹 Type "WHO standards" – Water quality limits
🔹 Type "pH", "TDS", or "turbidity" – Parameter info

Or use the quick action buttons above! 👆"""
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text = text, isBot = false, timestamp = System.currentTimeMillis()))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.smoothScrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text = text, isBot = true, timestamp = System.currentTimeMillis()))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.smoothScrollToPosition(messages.size - 1)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
