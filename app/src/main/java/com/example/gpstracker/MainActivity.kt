package com.example.gpstracker

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpstracker.databinding.ActivityMainBinding
import com.example.gpstracker.fragments.MainFragment1
import com.example.gpstracker.fragments.SettingFragment
import com.example.gpstracker.fragments.TracksFragment
import com.example.gpstracker.utils.FragmentManager
import com.example.gpstracker.utils.openFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //openFragment(MainFragment.newInstance())
        onBottomNavClick()
        if (savedInstanceState == null) {
            FragmentManager.setFragment(MainFragment1.newInstance(),this)
        }
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (FragmentManager.currentFragment is MainFragment1) finish()
                else FragmentManager.setFragment(MainFragment1.newInstance(),this@MainActivity)
            }

        })

    }
    private fun onBottomNavClick(){
        binding.bNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.id_home->{
                    openFragment(MainFragment1.newInstance())
                }
                R.id.id_tracks->{
                    openFragment(TracksFragment.newInstance())
                }
                R.id.id_settings->{
                    openFragment(SettingFragment())
                }
            }
            true
        }
    }


}