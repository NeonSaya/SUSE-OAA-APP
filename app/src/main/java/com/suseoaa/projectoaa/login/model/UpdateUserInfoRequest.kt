package com.suseoaa.projectoaa.login.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserInfoRequest(
    @Json(name = "studentid")
    val studentid: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "department")
    val department: String,

    @Json(name = "role")
    val role: String
)