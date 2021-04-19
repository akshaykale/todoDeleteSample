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

            timer = object : CountDownTimer(TasksFragment.UNDO_TIMER, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    val per = (millisUntilFinished.toFloat().div(TasksFragment.UNDO_TIMER.toFloat())).times(100)
                    binding.progressBar.progress = (100 - per).toInt()
                }

                override fun onFinish() {
                    if (!isTicking) return //its not ticking (may be undo has been pressed)
                    processDelete(item, position)

                }
            }

            binding.deleteBt.setOnClickListener {
                isTicking = true // start the timer
                timer?.start()
                viewStateDeleting()
            }

            binding.undoButton.setOnClickListener {
                if (isTicking) {
                    isTicking = false //imp. because calling cancle() won't prevent a onFinish() call on timer finished.
                    timer?.cancel()
                }
                viewStateReadyToDelete()
            }

            bind(viewModel, item)
        }
    }

    private fun ViewHolder.viewStateDeleting() {
        binding.deleteBt.gone()
        binding.progressBar.visible()
        binding.undoButton.visible()
    }

    private fun ViewHolder.viewStateReadyToDelete() {
        binding.undoButton.gone()
        binding.deleteBt.visible()
        binding.progressBar.gone()
    }

    private fun ViewHolder.processDelete(item: Task, position: Int) {
        viewModel.deleteTask(item)
        viewStateReadyToDelete()
        isTicking = false
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
