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

package com.dacode.android.kotlin.githubapi.data

import com.dacode.android.kotlin.githubapi.api.GithubService
import com.dacode.android.kotlin.githubapi.model.Issue
import com.dacode.android.kotlin.githubapi.model.IssueResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import retrofit2.HttpException
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 1

/**
 * Repository class that works with local and remote data sources.
 */
@ExperimentalCoroutinesApi
class IssueRepository(private val service: GithubService) {

    // keep the list of all results received
    private val inMemoryCache = mutableListOf<Issue>()

    // keep channel of results. The channel allows us to broadcast updates so
    // the subscriber will have the latest data
    private val searchResults = ConflatedBroadcastChannel<IssueResult>()

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = GITHUB_STARTING_PAGE_INDEX

    // avoid triggering multiple requests in the same time
    private var isRequestInProgress = false

    // text filter on title or description
    private var searchFilter = ""

    // closed filter set here just in case there is a isRequestInProgress so we can filter right after
    private var closedFilter = true

    // open filter set here just in case there is a isRequestInProgress so we can filter right after
    private var openFilter = true

    // mark last page for scrolling + filtering
    private var lastpage = false

    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    suspend fun getIssuesStream(query: String, url: String?): Flow<IssueResult> {
        lastpage = false
        searchFilter = ""
        lastRequestedPage = 1
        closedFilter = true
        openFilter = true
        inMemoryCache.clear()
        val successful = requestAndSaveData(query, url)
        if (successful) {
            lastRequestedPage++
        }
        return searchResults.asFlow()
    }

    suspend fun requestMore(query: String, url: String?) {
        if (isRequestInProgress || lastpage) return
        val successful = requestAndSaveData(query, url)
        if (successful) {
            lastRequestedPage++
        }
    }

    suspend fun setClosed(query: String, closed: Boolean) {
        closedFilter = closed
        if (isRequestInProgress) return
        searchResults.offer(IssueResult.Success(reposByState()))
    }

    suspend fun setOpen(query: String, open: Boolean) {
        openFilter = open
        if (isRequestInProgress) return
        searchResults.offer(IssueResult.Success(reposByState()))
    }

    suspend fun setSearchString(query: String, search: String) {
        searchFilter = search
        if (isRequestInProgress) return
        searchResults.offer(IssueResult.Success(reposByState()))
    }

    suspend fun retry(query: String, url: String?) {
        if (isRequestInProgress) return
        requestAndSaveData(query, url)
    }

    private suspend fun requestAndSaveData(query: String, url: String?): Boolean {
        isRequestInProgress = true
        var successful = false

        //Api query in request would be done here
        //val apiQuery = query + IN_QUALIFIER
        try {
            val response = service.getIssues(url,"all", lastRequestedPage, NETWORK_PAGE_SIZE)
            //Log.d("GithubRepository", "response $response")
            val repos = response ?: emptyList()
            if (repos.isEmpty()) {lastpage = true}
            inMemoryCache.addAll(repos)
            val reposByName = reposByState()
            searchResults.offer(IssueResult.Success(reposByName))
            successful = true
        } catch (exception: IOException) {
            searchResults.offer(IssueResult.Error(exception))
        } catch (exception: HttpException) {
            searchResults.offer(IssueResult.Error(exception))
        }
        isRequestInProgress = false
        return successful
    }

    private fun reposByState(): List<Issue> {
        // from the in memory cache select only the issues whose name or description matches the searchfilter and states
        if (closedFilter && !openFilter) {
            if (searchFilter.isNotEmpty()) {
                return inMemoryCache.filter { it.state!!.contains("closed", true) && (it.fullName.contains(searchFilter, true) || (it.description != null && it.description.contains(searchFilter, true))) }
            } else {
                return inMemoryCache.filter { it.state!!.contains("closed", true) }
            }
        } else if (!closedFilter && openFilter) {
            if (searchFilter.isNotEmpty()) {
                return inMemoryCache.filter { it.state!!.contains("open", true) && (it.fullName.contains(searchFilter, true) || (it.description != null && it.description.contains(searchFilter, true))) }
            } else {
                return inMemoryCache.filter { it.state!!.contains("open", true) }
            }
        } else if (!closedFilter && !openFilter) {
            return mutableListOf<Issue>()
        } else {
            if (searchFilter.isNotEmpty()) {
                return inMemoryCache.filter { it.fullName.contains(searchFilter, true) || (it.description != null && it.description.contains(searchFilter, true)) }
            } else {
                return inMemoryCache
            }
        }
    }

    companion object {
        //100 size is max for issues, since api rate limit based on ip is 60, might as well max this when possible
        private const val NETWORK_PAGE_SIZE = 100
    }
}
