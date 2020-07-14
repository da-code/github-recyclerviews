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

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dacode.android.kotlin.githubapi.Injection
import com.dacode.android.kotlin.githubapi.databinding.FragmentIssueListBinding
import com.dacode.android.kotlin.githubapi.model.IssueResult
import com.dacode.android.kotlin.githubapi.ui.issue.IssueListFragmentArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class IssueListFragment : Fragment() {

    private lateinit var binding: FragmentIssueListBinding
    private lateinit var viewModel: IssueListViewModel
    private val adapter = IssueAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIssueListBinding.inflate(layoutInflater)
        val view = binding.root
        //setLifecycleOwner()
        //setContentView(view)
        // get the view model
        viewModel = ViewModelProvider(this, Injection.provideIssueViewModelFactory())
                .get(IssueListViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var url = arguments?.let { IssueListFragmentArgs.fromBundle(it).url }

        url = url?.replace("{/number}", "")
        if (url != null) viewModel.setUrl(url)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)
        setupScrollListener()

        initAdapter()

        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        //val state = savedInstanceState?.getString(LAST_SEARCH_STATE) ?: DEFAULT_STATE

        if (viewModel.issueResult.value == null) {
            viewModel.requestIssues(query)
        }

        initToggles()
        initSearch()

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, binding.searchIssues.text.trim().toString())
        //outState.putString(LAST_SEARCH_STATE, binding.searchIssues.text.trim().toString())
    }

    private fun initAdapter() {
        binding.list.adapter = adapter
        viewModel.issueResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is IssueResult.Success -> {
                    showEmptyList(result.data.isEmpty())
                    adapter.submitList(result.data)
                }
                is IssueResult.Error -> {
                    Toast.makeText(
                            context,
                            "\uD83D\uDE28 Wooops $result.message}",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun initSearch() {

        //binding.searchIssues.setText(query)

        binding.searchIssues.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateIssueListFromInput()
                true
            } else {
                false
            }
        }
        binding.searchIssues.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateIssueListFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun initToggles() {

        binding.switchButtonOpen.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.list.scrollToPosition(0)
            viewModel.setOpenFilter(isChecked)
        }

        binding.switchButtonClosed.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.list.scrollToPosition(0)
            viewModel.setClosedFilter(isChecked)
        }
    }

    private fun updateIssueListFromInput() {
        binding.searchIssues.text.trim().let {
            if (it.isNotEmpty()) {
                binding.list.scrollToPosition(0)
                viewModel.setIssueFilter(it.toString())
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.list.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
    }

    private fun setupScrollListener() {
        val layoutManager = binding.list.layoutManager as LinearLayoutManager
        binding.list.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                viewModel.listScrolled(visibleItemCount, lastVisibleItem, totalItemCount)
            }
        })
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "LAST_SEARCH_QUERY"
        private const val DEFAULT_QUERY = ""

    }
}
