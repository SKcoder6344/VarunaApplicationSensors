package com.varuna.app.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuna.app.adapters.EducationAdapter
import com.varuna.app.databinding.ActivityEducationBinding
import com.varuna.app.model.EducationItem

class EducationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEducationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEducationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Education & Guidelines"

        setupTabs()
        loadWHOContent()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("WHO Guidelines"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("BIS Standards"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Diseases"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Purification"))

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                when (tab.position) {
                    0 -> loadWHOContent()
                    1 -> loadBISContent()
                    2 -> loadDiseaseContent()
                    3 -> loadPurificationContent()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun loadWHOContent() {
        val items = listOf(
            EducationItem("WHO Water Quality Guidelines", """
The World Health Organization (WHO) Guidelines for Drinking-Water Quality provide a framework for protecting public health from the risks associated with drinking contaminated water.

Key WHO Standards:
• pH: 6.5–8.5 (no health-based guideline value; outside this range, water becomes corrosive or scaling)
• Turbidity: <4 NTU (viruses may not be adequately disinfected above 1 NTU)
• TDS: 600 mg/L (palatability guideline; no direct health basis)
• Arsenic: 0.01 mg/L (carcinogenic at higher levels)
• Fluoride: 1.5 mg/L (dental and skeletal fluorosis risk above this)
• Nitrate: 50 mg/L (methaemoglobinaemia in infants)
• Lead: 0.01 mg/L (neurotoxic especially for children)
• E. coli: 0 per 100 mL (no E. coli should be detected in drinking water)

WHO recommends a multi-barrier approach:
1. Source protection
2. Treatment
3. Safe distribution and storage
4. Consumer practices
            """.trimIndent()),
            EducationItem("Water Safety Plans (WSP)", """
WHO promotes Water Safety Plans as the most effective way to ensure drinking water safety.

Components:
1. System assessment – Identify hazards from source to tap
2. Monitoring – Operational monitoring at critical control points
3. Management – Corrective actions and verification
4. Supporting programmes – Training, consumer communication, documentation

Benefits:
• Proactive rather than reactive approach
• Identifies risks before contamination occurs
• Improves water utility performance
• Increases consumer confidence
            """.trimIndent())
        )
        setupRecyclerView(items)
    }

    private fun loadBISContent() {
        val items = listOf(
            EducationItem("BIS IS 10500:2012 – Indian Drinking Water Standards", """
The Bureau of Indian Standards (BIS) IS 10500:2012 specifies requirements for drinking water in India.

Physical Parameters:
• Colour: ≤5 Hazen units (desirable)
• Turbidity: ≤1 NTU (desirable), ≤5 NTU (permissible)
• pH: 6.5–8.5
• Total Dissolved Solids (TDS): ≤500 mg/L (desirable), ≤2000 mg/L (permissible)
• Total Hardness (as CaCO₃): ≤200 mg/L (desirable), ≤600 mg/L (permissible)

Chemical Parameters:
• Arsenic: ≤0.01 mg/L (no relaxation)
• Fluoride: ≤1.0 mg/L (desirable), ≤1.5 mg/L (permissible)
• Chloride: ≤250 mg/L (desirable), ≤1000 mg/L (permissible)
• Nitrate: ≤45 mg/L (no relaxation)
• Iron: ≤0.3 mg/L (desirable)
• Manganese: ≤0.1 mg/L (desirable)
• Lead: ≤0.01 mg/L (no relaxation)

Bacteriological Parameters:
• Coliform organisms: absent in 100 mL
• E. coli/thermotolerant coliform: absent in 100 mL
            """.trimIndent())
        )
        setupRecyclerView(items)
    }

    private fun loadDiseaseContent() {
        val items = listOf(
            EducationItem("Cholera", """
Cholera is an acute diarrhoeal infection caused by Vibrio cholerae bacteria.

Transmission: Contaminated water or food
Symptoms: Profuse watery diarrhoea, vomiting, muscle cramps, dehydration
Incubation: 2 hours – 5 days

Prevention:
✓ Drink only boiled or treated water
✓ Wash hands with soap thoroughly
✓ Avoid ice made from tap water
✓ Avoid raw/undercooked seafood
✓ Oral cholera vaccines available

Treatment:
• Immediate rehydration with ORS
• IV fluids for severe cases
• Antibiotics (reduces severity)
• Zinc supplementation for children
            """.trimIndent()),
            EducationItem("Typhoid Fever", """
Typhoid is caused by Salmonella typhi bacteria through contaminated water/food.

Symptoms: High fever, headache, stomach pain, constipation or diarrhoea
Incubation: 1-3 weeks

Prevention:
✓ Safe water and food hygiene
✓ Typhoid vaccination
✓ Proper sewage disposal
✓ Avoid food from unhygienic sources

Treatment:
• Antibiotics (ciprofloxacin, azithromycin)
• Adequate rest and hydration
• Hospitalisation for severe cases
            """.trimIndent()),
            EducationItem("Diarrhoeal Diseases", """
Diarrhoea is one of the leading causes of child mortality worldwide.

Common pathogens in water:
• Rotavirus
• Cryptosporidium
• Giardia
• E. coli strains
• Shigella

Prevention:
✓ Boil drinking water
✓ ORS (Oral Rehydration Salts) preparation
✓ Handwashing at critical times
✓ Breastfeeding infants
✓ Rotavirus vaccination

ORS Recipe (Home made):
1 liter clean water + 6 level teaspoons sugar + 1/2 teaspoon salt
Stir until dissolved. Give small sips frequently.
            """.trimIndent())
        )
        setupRecyclerView(items)
    }

    private fun loadPurificationContent() {
        val items = listOf(
            EducationItem("Household Water Treatment Methods", """
1. BOILING (Most Reliable)
• Bring water to rolling boil for 1 min (3 min at altitude)
• Let cool in clean covered container
• Effective against: bacteria, viruses, protozoa
• Does NOT remove chemical contaminants

2. CHLORINATION
• Add 2 drops of 5% sodium hypochlorite per liter
• Wait 30 minutes before drinking
• Effective against: bacteria, most viruses

3. SOLAR DISINFECTION (SODIS)
• Fill clear PET bottles with water
• Place in sunlight for 6 hours (2 days if cloudy)
• Safe and free method

4. CERAMIC/SAND FILTERS
• Removes turbidity and some pathogens
• Combine with chlorination for best results

5. REVERSE OSMOSIS (RO)
• Removes TDS, heavy metals, bacteria, viruses
• Best for high TDS water
• Wastes 3-4 liters per 1 liter purified

6. UV DISINFECTION
• UV light kills bacteria and viruses
• Requires clear water (<1 NTU)
• No chemical taste or smell added
            """.trimIndent())
        )
        setupRecyclerView(items)
    }

    private fun setupRecyclerView(items: List<EducationItem>) {
        val adapter = EducationAdapter(items)
        binding.rvEducation.layoutManager = LinearLayoutManager(this)
        binding.rvEducation.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressed(); return true }
}
