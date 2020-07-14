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

import android.util.Log
import com.dacode.android.kotlin.githubapi.api.GithubService
import com.dacode.android.kotlin.githubapi.api.IN_QUALIFIER
import com.dacode.android.kotlin.githubapi.model.IssueResult
import com.dacode.android.kotlin.githubapi.model.Repo
import com.dacode.android.kotlin.githubapi.model.RepoSearchResult
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
class GithubRepository(private val service: GithubService) {

    // keep the list of all results received
    private val inMemoryCache = mutableListOf<Repo>()

    // keep channel of results. The channel allows us to broadcast updates so
    // the subscriber will have the latest data
    private val searchResults = ConflatedBroadcastChannel<RepoSearchResult>()

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = GITHUB_STARTING_PAGE_INDEX

    // avoid triggering multiple requests in the same time
    private var isRequestInProgress = false

    // text filter on title or description
    private var searchFilter = ""

    // mark last page for scrolling + filtering
    private var lastpage = false

    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    suspend fun getSearchResultStream(query: String): Flow<RepoSearchResult> {
        //Log.d("GithubRepository", "New query: $query")
        lastpage = false
        searchFilter = ""
        lastRequestedPage = 1
        inMemoryCache.clear()
        val successful = requestAndSaveData(query)
        if (successful) {
            lastRequestedPage++
        }
        return searchResults.asFlow()
    }

    suspend fun requestMore(query: String) {
        if (isRequestInProgress || lastpage) return
        val successful = requestAndSaveData(query)
        if (successful) {
            lastRequestedPage++
        }
    }

    suspend fun setSearchString(query: String, search: String) {
        searchFilter = search
        if (isRequestInProgress) return
        searchResults.offer(RepoSearchResult.Success(reposByName()))
    }

    suspend fun retry(query: String) {
        if (isRequestInProgress) return
        requestAndSaveData(query)
    }

    private suspend fun requestAndSaveData(query: String): Boolean {
        isRequestInProgress = true
        var successful = false

        //val apiQuery = query + IN_QUALIFIER
        try {
            val response = service.searchRepos("all", lastRequestedPage, NETWORK_PAGE_SIZE)
            //Log.d("GithubRepository", "response $response")
            val repos = response ?: emptyList()
            if (repos.isEmpty()) {lastpage = true}
            inMemoryCache.addAll(repos)
            val reposByName = reposByName()
            searchResults.offer(RepoSearchResult.Success(reposByName))
            successful = true
        } catch (exception: IOException) {
            searchResults.offer(RepoSearchResult.Error(exception))
        } catch (exception: HttpException) {
            searchResults.offer(RepoSearchResult.Error(exception))
        }
        isRequestInProgress = false
        return successful
    }

    private fun reposByName(): List<Repo> {
        // from the in memory cache select only the repos whose name or description matches
        // the query. Then order the results.
        if (searchFilter.isNotEmpty()) {
            return inMemoryCache.filter {it.name.contains(searchFilter, true) || (it.description != null && it.description.contains(searchFilter, true))}
        } else {
            return inMemoryCache
        }
    }

    companion object {
        //100 size is max, since api rate limit is 60, might as well max this
        private const val NETWORK_PAGE_SIZE = 100
    }
}
