package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Holds the API calls for the Taskie app.
 */
interface RemoteApiService {

  @POST("/api/register")
  suspend fun registerUser(@Body request: UserDataRequest): RegisterResponse

  @GET("/api/note")
  suspend fun getNotes(): GetTasksResponse

  @POST("/api/login")
  suspend fun loginUser(@Body request: UserDataRequest): LoginResponse

  @GET("/api/user/profile")
  suspend fun getMyProfile(): UserProfileResponse

  @POST("/api/note/complete")
  suspend fun completeTask(@Query("id") noteId: String): CompleteNoteResponse

  @POST("/api/note")
  suspend fun addTask(@Body request: AddTaskRequest): Task

  @DELETE("/api/note")
  fun deleteNote(@Query("id") noteId: String): DeleteNoteResponse
}