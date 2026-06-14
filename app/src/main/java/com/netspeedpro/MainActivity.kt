package com.netspeedpro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.netspeedpro.databinding.ActivityMainBinding
import com.netspeedpro.ui.HistoryFragment
import com.netspeedpro.ui.NetworkFragment
import com.netspeedpro.ui.SpeedTestFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf(
            SpeedTestFragment() as Fragment,
            NetworkFragment() as Fragment,
            HistoryFragment() as Fragment
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = pages.size
            override fun createFragment(position: Int) = pages[position]
        }
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Speed Test"
                1 -> "Network"
                else -> "History"
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_speedometer
                1 -> R.drawable.ic_wifi
                else -> R.drawable.ic_history
            })
        }.attach()
    }
}
