package com.netspeedpro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.netspeedpro.AppDatabase
import com.netspeedpro.SpeedTestManager
import com.netspeedpro.TestResult
import com.netspeedpro.databinding.FragmentSpeedTestBinding
import kotlinx.coroutines.launch
import java.util.UUID

class SpeedTestFragment : Fragment() {

    private var _binding: FragmentSpeedTestBinding? = null
    private val binding get() = _binding!!

    private var isRunning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSpeedTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetUI()
        binding.btnStart.setOnClickListener { if (!isRunning) startTest() }
    }

    private fun resetUI() {
        binding.tvDownloadVal.text = "—"
        binding.tvUploadVal.text = "—"
        binding.tvPingVal.text = "—"
        binding.speedGauge.setSpeed(0f, animate = false)
        binding.speedGauge.label = "TAP TO TEST"
        binding.speedGauge.gaugeColor = 0xFF00D4FF.toInt()
        binding.speedGauge.invalidate()
        binding.btnStart.text = "START TEST"
        binding.btnStart.isEnabled = true
        binding.tvStatus.text = ""
        setPhaseIndicators(-1)
    }

    private fun startTest() {
        isRunning = true
        binding.btnStart.isEnabled = false
        binding.btnStart.text = "RUNNING…"
        binding.tvDownloadVal.text = "—"
        binding.tvUploadVal.text = "—"
        binding.tvPingVal.text = "—"
        binding.tvStatus.text = "Starting test…"

        lifecycleScope.launch {
            try {
                setPhase(0, "Ping")
                val ping = SpeedTestManager.runPingTest()
                binding.tvPingVal.text = "$ping ms"
                binding.speedGauge.gaugeColor = 0xFFFFD700.toInt()
                binding.speedGauge.setSpeed(0f, animate = false)

                setPhase(1, "Download")
                binding.speedGauge.label = "DOWNLOAD"
                binding.speedGauge.gaugeColor = 0xFF00F590.toInt()
                val download = SpeedTestManager.runDownloadTest { speed ->
                    requireActivity().runOnUiThread {
                        binding.speedGauge.setSpeed(speed)
                        binding.tvStatus.text = "↓ %.1f Mbps".format(speed)
                    }
                }
                binding.tvDownloadVal.text = formatSpeed(download)
                binding.speedGauge.setSpeed(download)

                setPhase(2, "Upload")
                binding.speedGauge.label = "UPLOAD"
                binding.speedGauge.gaugeColor = 0xFFFF6B35.toInt()
                binding.speedGauge.setSpeed(0f, animate = false)
                val upload = SpeedTestManager.runUploadTest { speed ->
                    requireActivity().runOnUiThread {
                        binding.speedGauge.setSpeed(speed)
                        binding.tvStatus.text = "↑ %.1f Mbps".format(speed)
                    }
                }
                binding.tvUploadVal.text = formatSpeed(upload)

                setPhase(3, "Done")
                binding.tvStatus.text = "Test complete"
                binding.speedGauge.label = "DOWNLOAD"
                binding.speedGauge.gaugeColor = 0xFF00F590.toInt()
                binding.speedGauge.setSpeed(download)

                val result = TestResult(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    downloadMbps = download,
                    uploadMbps = upload,
                    pingMs = ping,
                    networkType = "—",
                    carrier = "—",
                    generation = "—"
                )
                AppDatabase.getInstance(requireContext()).testResultDao().insert(result)

                binding.btnStart.text = "TEST AGAIN"
                binding.btnStart.isEnabled = true
            } catch (e: Exception) {
                binding.tvStatus.text = "Error: ${e.message}"
                binding.speedGauge.label = "FAILED"
                binding.btnStart.text = "RETRY"
                binding.btnStart.isEnabled = true
                setPhaseIndicators(-1)
            } finally {
                isRunning = false
            }
        }
    }

    private fun setPhase(phase: Int, name: String) {
        requireActivity().runOnUiThread {
            binding.tvStatus.text = "Testing $name…"
            setPhaseIndicators(phase)
        }
    }

    private fun setPhaseIndicators(activePhase: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { i, dot ->
            dot.setBackgroundColor(
                when {
                    i < activePhase -> 0x8800D4FF.toInt()
                    i == activePhase -> 0xFF00D4FF.toInt()
                    else -> 0xFF162035.toInt()
                }
            )
        }
    }

    private fun formatSpeed(mbps: Float): String =
        if (mbps >= 100f) "%.0f Mbps".format(mbps) else "%.1f Mbps".format(mbps)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
