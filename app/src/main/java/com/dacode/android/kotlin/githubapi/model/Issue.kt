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

package com.dacode.android.kotlin.githubapi.model

import com.google.gson.annotations.SerializedName

/**
 * Immutable model class for a Github issue that holds all the information about an issue.
 * Objects of this type are received from the Github API, therefore all the fields are annotated
 * with the serialized name.
 * This class also defines the Room repos table, where the issue [id] is the primary key.
 */
data class Issue(
    @field:SerializedName("id") val id: Long,
    @field:SerializedName("title") val fullName: String,
    @field:SerializedName("body") val description: String?,
    @field:SerializedName("state") val state: String?,
    @field:SerializedName("user") val owner: Owner,
    @field:SerializedName("comments") val comments: Int
)
