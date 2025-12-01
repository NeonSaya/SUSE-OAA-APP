package com.suseoaa.projectoaa.login.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfoData(
    @Json(name = "studentid")
    val studentid: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "avatar")
    val avatar: String?,

    @Json(name = "department")
    val department: String,

    @Json(name = "role")
    val role: String
)

@JsonClass(generateAdapter = true)
data class UserInfoResponse(
    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: UserInfoData?
)