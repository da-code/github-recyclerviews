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

import androidx.lifecycle.*
import com.dacode.android.kotlin.githubapi.data.IssueRepository
import com.dacode.android.kotlin.githubapi.model.IssueResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * ViewModel for the [IssueListFragment] screen.
 * The ViewModel works with the [IssueRepository] to get the data.
 */
@ExperimentalCoroutinesApi
class IssueListViewModel(private val repository: IssueRepository) : ViewModel() {

    companion object {
        private const val VISIBLE_THRESHOLD = 9
        private var urlstring: String? = ""
        private var closedFilter: Boolean = true
        private var openFilter: Boolean = true
    }

    private val queryLiveData = MutableLiveData<String>()

    val issueResult: LiveData<IssueResult> = queryLiveData.switchMap { queryString ->
        liveData {
            val issues = repository.getIssuesStream(queryString, urlstring).asLiveData(Dispatchers.Main)
            emitSource(issues)
        }
    }

    /**
     * Set url for issues.
     */
    fun setUrl(url: String?) {
        urlstring = url;
    }

    /**
     * Start api request for issues.
     */
    fun requestIssues(queryString: String) {
        queryLiveData.postValue(queryString)
    }

    /**
     * Filter with searchString
     */
    fun setIssueFilter(searchString: String) {
        val immutableQuery = queryLiveData.value
        if (immutableQuery != null) {
            viewModelScope.launch {
                repository.setSearchString(immutableQuery, searchString)
            }
        }
    }

    /**
     * Set open state filter
     */
    fun setOpenFilter(checked: Boolean) {
        openFilter = checked
        val immutableQuery = queryLiveData.value
        if (immutableQuery != null) {
            viewModelScope.launch {
                repository.setOpen(immutableQuery, checked)
            }
        }
    }

    /**
     * Set closed state filter
     */
    fun setClosedFilter(checked: Boolean) {
        closedFilter = checked
        val immutableQuery = queryLiveData.value
        if (immutableQuery != null) {
            viewModelScope.launch {
                repository.setClosed(immutableQuery, checked)
            }
        }
    }

    fun listScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            val immutableQuery = queryLiveData.value
            if (immutableQuery != null) {
                viewModelScope.launch {
                    repository.requestMore(immutableQuery, urlstring)
                }
            }
        }
    }
}