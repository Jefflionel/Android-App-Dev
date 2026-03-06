package com.example.gradecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gradecalculator.databinding.FragmentResultsBinding
import com.example.gradecalculator.util.ExcelHandler
import com.example.gradecalculator.util.GradeCalculator
import com.example.gradecalculator.viewmodel.GradeViewModel

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GradeViewModel by activityViewModels()
    private lateinit var adapter: StudentGradeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupExportButton()
    }

    private fun setupRecyclerView() {
        adapter = StudentGradeAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.studentGrades.observe(viewLifecycleOwner) { students ->
            adapter.submitList(students)
            binding.tvSummary.text = GradeCalculator.buildSummary(students)
            binding.tvEmptyState.visibility =
                if (students.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupExportButton() {
        binding.btnExport.setOnClickListener {
            val students = viewModel.studentGrades.value
            if (students.isNullOrEmpty()) {
                showToast("No data to export")
                return@setOnClickListener
            }
            ExcelHandler.exportToExcel(
                context   = requireContext(),
                students  = students,
                onSuccess = { fileName -> showToast("Saved to Downloads: $fileName") },
                onError   = { error    -> showToast("Export failed: $error") }
            )
        }
    }

    private val showToast: (String) -> Unit = { msg ->
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}