package com.netspeedpro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.netspeedpro.NetworkInfoManager
import com.netspeedpro.databinding.FragmentNetworkBinding
import kotlinx.coroutines.launch

class NetworkFragment : Fragment() {

    private var _binding: FragmentNetworkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNetworkInfo()
        binding.btnRefresh.setOnClickListener { loadNetworkInfo() }
    }

    private fun loadNetworkInfo() {
        binding.tvStatus.text = "Detecting…"
        lifecycleScope.launch {
            val info = NetworkInfoManager.getNetworkInfo(requireContext())

            binding.tvStatus.text = if (info.isConnected) "● Connected" else "○ Disconnected"
            binding.tvStatus.setTextColor(
                if (info.isConnected) 0xFF00F590.toInt() else 0xFFFF4D4D.toInt()
            )
            binding.tvNetworkBadge.text = info.networkType
            binding.tvNetworkType.text = info.networkType
            binding.tvCarrier.text = info.carrier
            binding.tvGeneration.text = info.generation
            binding.tvIpAddress.text = info.ipAddress
            binding.tvInternetAccess.text = if (info.isInternetAvailable) "✓ Available" else "✗ Unavailable"
            binding.tvInternetAccess.setTextColor(
                if (info.isInternetAvailable) 0xFF00F590.toInt() else 0xFFFF4D4D.toInt()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
