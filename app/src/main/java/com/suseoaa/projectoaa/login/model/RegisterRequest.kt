package com.suseoaa.projectoaa.login.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "studentid")
    val studentid: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "role")
    val role: String
)