package com.example.gradecalculator.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gradecalculator.databinding.ItemStudentGradeBinding
import com.example.gradecalculator.model.StudentGrade

class StudentGradeAdapter :
    ListAdapter<StudentGrade, StudentGradeAdapter.GradeViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<StudentGrade>() {
        override fun areItemsTheSame(old: StudentGrade, new: StudentGrade) =
            old.name == new.name && old.mark == new.mark
        override fun areContentsTheSame(old: StudentGrade, new: StudentGrade) = old == new
    }

    private val gradeColorMap: (String) -> Int = { grade ->
        when (grade) {
            "A"  -> android.graphics.Color.parseColor("#2E7D32")
            "B"  -> android.graphics.Color.parseColor("#1565C0")
            "C"  -> android.graphics.Color.parseColor("#F57F17")
            "D"  -> android.graphics.Color.parseColor("#E65100")
            else -> android.graphics.Color.parseColor("#B71C1C")
        }
    }

    inner class GradeViewHolder(private val binding: ItemStudentGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val bind: (StudentGrade) -> Unit = { student ->
            binding.tvName.text  = student.name
            binding.tvMark.text  = student.mark.toInt().toString()
            binding.tvGrade.text = student.grade
            binding.tvGrade.setTextColor(gradeColorMap(student.grade))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val binding = ItemStudentGradeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) =
        holder.bind(getItem(position))
}