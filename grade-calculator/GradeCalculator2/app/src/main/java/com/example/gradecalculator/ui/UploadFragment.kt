package com.example.gradecalculator.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.gradecalculator.R
import com.example.gradecalculator.databinding.FragmentUploadBinding
import com.example.gradecalculator.util.ExcelHandler
import com.example.gradecalculator.util.GradeCalculator
import com.example.gradecalculator.viewmodel.GradeViewModel

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GradeViewModel by activityViewModels()
    private var selectedFileUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let(::handleFileSelected)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSelectFile.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            }.also { filePickerLauncher.launch(it) }
        }
        binding.btnProcessFile.setOnClickListener {
            selectedFileUri?.let(::processFile) ?: showToast("Please select a file first")
        }
    }

    private fun observeViewModel() {
        viewModel.selectedFileName.observe(viewLifecycleOwner) { name ->
            binding.tvFileName.text = if (name.isBlank()) "No file selected" else "File: $name"
            binding.btnProcessFile.isEnabled = name.isNotBlank()
        }
    }

    private fun handleFileSelected(uri: Uri) {
        selectedFileUri = uri
        val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Selected file"
        viewModel.updateFileName(fileName)
    }

    private fun processFile(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnProcessFile.isEnabled = false
        val rawEntries = mutableListOf<Pair<String, Double>>()

        ExcelHandler.readStudentsFromUri(
            context     = requireContext(),
            uri         = uri,
            onRowParsed = { name, mark -> rawEntries.add(name to mark) },
            onError     = { error ->
                binding.progressBar.visibility = View.GONE
                binding.btnProcessFile.isEnabled = true
                showToast("Error: $error")
            }
        )

        if (rawEntries.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            binding.btnProcessFile.isEnabled = true
            showToast("No valid data found. Check columns: Name, Mark")
            return
        }

        val results = GradeCalculator.processStudents(rawEntries)
        viewModel.updateGrades(results)
        binding.progressBar.visibility = View.GONE
        findNavController().navigate(R.id.action_uploadFragment_to_resultsFragment)
    }

    private val showToast: (String) -> Unit = { message ->
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}