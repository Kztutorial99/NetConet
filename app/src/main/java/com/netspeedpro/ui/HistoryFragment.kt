package com.netspeedpro.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netspeedpro.AppDatabase
import com.netspeedpro.TestResult
import com.netspeedpro.R
import com.netspeedpro.databinding.FragmentHistoryBinding
import com.netspeedpro.databinding.ItemHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val adapter = HistoryAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.btnClearAll.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).testResultDao().deleteAll()
            }
        }

        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext()).testResultDao().getAll()
                .collectLatest { list ->
                    adapter.submitList(list)
                    binding.emptyGroup.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.recycler.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    binding.btnClearAll.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private var items: List<TestResult> = emptyList()
    private val fmt = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())

    fun submitList(list: List<TestResult>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(private val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: TestResult) {
            b.tvDate.text = fmt.format(Date(r.timestamp))
            b.tvDownload.text = formatMbps(r.downloadMbps)
            b.tvUpload.text = formatMbps(r.uploadMbps)
            b.tvPing.text = "${r.pingMs} ms"
        }
        private fun formatMbps(v: Float) =
            if (v >= 100f) "%.0f Mbps".format(v) else "%.1f Mbps".format(v)
    }
}
