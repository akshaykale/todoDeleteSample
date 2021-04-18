/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.tasks

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding
import com.example.android.architecture.blueprints.todoapp.tasks.TasksAdapter.ViewHolder

/**
 * Adapter for the task list. Has a reference to the [TasksViewModel] to send actions back to it.
 */
class TasksAdapter(private val viewModel: TasksViewModel) :
        ListAdapter<Task, ViewHolder>(TaskDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.apply {

            if (timer != null) {
                timer?.cancel()
            }

            timer = object : CountDownTimer(3000, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.timerText.text = "Undo in ${((millisUntilFinished) / 1).toInt()}..."
                }

                override fun onFinish() {
                    if (!isTicking) return
                    viewModel.deleteTask(item)
                    binding.deleteContainer.gone()
                    binding.deleteBt.visible()
                    removeItem(position, item)

                }
            }

            binding.deleteBt.setOnClickListener {
                isTicking = true
                timer?.start()
                it.gone()
                binding.deleteContainer.visible()
            }

            binding.undoButton.setOnClickListener {
                if (isTicking) {
                    isTicking = false //imp. because calling cancle() wont prevent a onFinish() call on timer finished.
                    timer?.cancel()
                }
                binding.deleteContainer.visibility = View.GONE
            }

            bind(viewModel, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: TaskItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        var timer: CountDownTimer? = null
        var isTicking: Boolean = false


        fun bind(viewModel: TasksViewModel, item: Task) {

            binding.viewmodel = viewModel
            binding.task = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    fun removeItem(position: Int, task: Task) {
        if (currentList.size > position && task.id != currentList[position].id) { // Some mismatch we should not remove
            return
        }
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, currentList.size)
    }
}

private fun View.visible() {
    visibility = View.VISIBLE
}

private fun View.gone() {
    visibility = View.GONE
}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
