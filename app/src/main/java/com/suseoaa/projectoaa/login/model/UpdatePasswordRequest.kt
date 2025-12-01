package com.suseoaa.projectoaa.login.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequest(
    @Json(name = "oldPassword")
    val oldPassword: String,

    @Json(name = "newPassword")
    val newPassword: String
)