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

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dacode.android.kotlin.githubapi.R
import com.dacode.android.kotlin.githubapi.model.Issue

/**
 * View Holder for a [Issue] RecyclerView list item.
 */
class IssueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.issue_name)
    private val description: TextView = view.findViewById(R.id.issue_description)
    private val issueState: TextView = view.findViewById(R.id.issue_state)
    private val comments: TextView = view.findViewById(R.id.comments_count)

    private var issue: Issue? = null

    init {
        view.setOnClickListener {
        }
    }

    fun bind(issue: Issue?) {
        if (issue == null) {
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
            description.visibility = View.GONE
            //language.visibility = View.GONE
            issueState.text = resources.getString(R.string.unknown)
            comments.text = resources.getString(R.string.unknown)
        } else {
            showIssueData(issue)
        }
    }

    private fun showIssueData(issue: Issue) {
        this.issue = issue
        name.text = issue.fullName

        // if the description is missing, hide the TextView
        var descriptionVisibility = View.GONE
        if (issue.description != null) {
            description.text = issue.description
            descriptionVisibility = View.VISIBLE
        }
        description.visibility = descriptionVisibility

        val resources = this.itemView.context.resources
        issueState.text = resources.getString(R.string.issue_state, issue.state)
        comments.text = issue.comments.toString()

        // if the language is missing, hide the label and the value
        //var languageVisibility = View.GONE
        //if (!issue.language.isNullOrEmpty()) {
        //    val resources = this.itemView.context.resources
        //    language.text = resources.getString(R.string.language, issue.language)
        //    languageVisibility = View.VISIBLE
        //}
        //language.visibility = languageVisibility
    }

    companion object {
        fun create(parent: ViewGroup): IssueViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.issue_view_item, parent, false)
            return IssueViewHolder(view)
        }
    }
}
