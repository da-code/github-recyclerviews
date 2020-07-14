/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.dacode.android.kotlin.githubapi.ui.issue

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dacode.android.kotlin.githubapi.model.Issue

/**
 * Adapter for the list of issues.
 */
class IssueAdapter : ListAdapter<Issue, androidx.recyclerview.widget.RecyclerView.ViewHolder>(ISSUE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return IssueViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val issueItem = getItem(position)
        if (issueItem != null) {
            (holder as IssueViewHolder).bind(issueItem)
        }
    }

    override fun submitList(list: List<Issue>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    companion object {
        private val ISSUE_COMPARATOR = object : DiffUtil.ItemCallback<Issue>() {
            override fun areItemsTheSame(oldItem: Issue, newItem: Issue): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Issue, newItem: Issue): Boolean =
                    oldItem == newItem
        }
    }
}
