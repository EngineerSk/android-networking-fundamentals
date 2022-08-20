/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.taskie.networking

import com.raywenderlich.android.taskie.App
import com.raywenderlich.android.taskie.model.Task
import com.raywenderlich.android.taskie.model.UserProfile
import com.raywenderlich.android.taskie.model.request.AddTaskRequest
import com.raywenderlich.android.taskie.model.request.UserDataRequest
import com.raywenderlich.android.taskie.model.response.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi(private val remoteApiService: RemoteApiService) {

    fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
        remoteApiService.loginUser(userDataRequest).enqueue(object : Callback<LoginResponse> {
            /**
             * Invoked for a received HTTP response.
             *
             *
             * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
             * Call [Response.isSuccessful] to determine if the response indicates success.
             */
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                if (responseBody == null) {
                    onUserLoggedIn(null, NullPointerException("Unable to login"))
                    return
                }
                if (responseBody.token.isNullOrBlank()) {
                    onUserLoggedIn(null, NullPointerException("Unable to authenticate user"))
                } else {
                    onUserLoggedIn(responseBody.token, null)
                }
            }

            /**
             * Invoked when a network exception occurred talking to the server or when an unexpected exception
             * occurred creating the request or processing the response.
             */
            override fun onFailure(call: Call<LoginResponse>, error: Throwable) {
                onUserLoggedIn(null, error)
            }
        })
    }

    fun registerUser(
        userDataRequest: UserDataRequest,
        onUserCreated: (String?, Throwable?) -> Unit
    ) {
        remoteApiService.registerUser(userDataRequest).enqueue(
            object : Callback<RegisterResponse> {
                /**
                 * Invoked for a received HTTP response.
                 *
                 *
                 * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                 * Call [Response.isSuccessful] to determine if the response indicates success.
                 */
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    val message = response.body()
                    if (message == null) {
                        onUserCreated(null, NullPointerException("No response body"))
                        return
                    }
                    onUserCreated(message.message, null)
                }
                /**
                 * Invoked when a network exception occurred talking to the server or when an unexpected exception
                 * occurred creating the request or processing the response.
                 */
                override fun onFailure(call: Call<RegisterResponse>, error: Throwable) {
                    onUserCreated(null, error)
                }

            }
        )
    }

    fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
        remoteApiService.getNotes(App.getToken()).enqueue(
            object : Callback<GetTasksResponse> {
                /**
                 * Invoked for a received HTTP response.
                 *
                 *
                 * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                 * Call [Response.isSuccessful] to determine if the response indicates success.
                 */
                override fun onResponse(
                    call: Call<GetTasksResponse>,
                    response: Response<GetTasksResponse>
                ) {
                    val jsonBody = response.body()
                    if (jsonBody == null) {
                        onTasksReceived(emptyList(), NullPointerException("No Data Available"))
                        return
                    }
                    if (jsonBody.notes.isNotEmpty()) {
                        onTasksReceived(jsonBody.notes.filter { !it.isCompleted }, null)
                    } else {
                        onTasksReceived(emptyList(), NullPointerException("No data available"))
                    }
                }

                /**
                 * Invoked when a network exception occurred talking to the server or when an unexpected exception
                 * occurred creating the request or processing the response.
                 */
                override fun onFailure(call: Call<GetTasksResponse>, error: Throwable) {
                    onTasksReceived(emptyList(), error)
                }
            }
        )
    }

    fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
        onTaskDeleted(null)
    }

    fun completeTask(taskId: String, onTaskCompleted: (Throwable?) -> Unit) {
        remoteApiService.completeTask(App.getToken(), taskId).enqueue(
            object : Callback<CompleteNoteResponse> {
                /**
                 * Invoked for a received HTTP response.
                 *
                 *
                 * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                 * Call [Response.isSuccessful] to determine if the response indicates success.
                 */
                override fun onResponse(
                    call: Call<CompleteNoteResponse>,
                    response: Response<CompleteNoteResponse>
                ) {
                    val jsonBody = response.body()
                    if (jsonBody == null) {
                        onTaskCompleted(NullPointerException("No response!"))
                        return
                    }

                    if (jsonBody.message == null) {
                        onTaskCompleted(NullPointerException("No response!"))
                    } else {
                        onTaskCompleted(null)
                    }
                }

                /**
                 * Invoked when a network exception occurred talking to the server or when an unexpected exception
                 * occurred creating the request or processing the response.
                 */
                override fun onFailure(call: Call<CompleteNoteResponse>, error: Throwable) {
                    onTaskCompleted(error)
                }

            }
        )
    }

    fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {
        remoteApiService.addTask(App.getToken(), addTaskRequest).enqueue(
            object : Callback<Task> {
                /**
                 * Invoked for a received HTTP response.
                 *
                 *
                 * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                 * Call [Response.isSuccessful] to determine if the response indicates success.
                 */
                override fun onResponse(
                    call: Call<Task>,
                    response: Response<Task>
                ) {
                    val jsonBody = response.body()
                    if (jsonBody == null) {
                        onTaskCreated(null, NullPointerException("No response!"))
                        return
                    }
                    onTaskCreated(jsonBody, null)
                }

                /**
                 * Invoked when a network exception occurred talking to the server or when an unexpected exception
                 * occurred creating the request or processing the response.
                 */
                override fun onFailure(call: Call<Task>, error: Throwable) {
                    onTaskCreated(null, error)
                }
            }
        )
    }

    fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
        getTasks { tasks, error ->
            if (error != null && error !is NullPointerException) {
                onUserProfileReceived(null, error)
                return@getTasks
            }

            remoteApiService.getUserProfile(App.getToken())
                .enqueue(object : Callback<UserProfileResponse> {
                    /**
                     * Invoked for a received HTTP response.
                     *
                     *
                     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                     * Call [Response.isSuccessful] to determine if the response indicates success.
                     */
                    override fun onResponse(
                        call: Call<UserProfileResponse>,
                        response: Response<UserProfileResponse>
                    ) {
                        val responseBody = response.body()
                        if (responseBody == null) {
                            onUserProfileReceived(null, NullPointerException("No data available"))
                            return
                        }
                        if (responseBody.email == null || responseBody.name == null) {
                            onUserProfileReceived(null, NullPointerException("No data available"))
                        } else {
                            onUserProfileReceived(
                                UserProfile(
                                    responseBody.email, responseBody.name,
                                    tasks.size
                                ), null
                            )
                        }
                    }

                    /**
                     * Invoked when a network exception occurred talking to the server or when an unexpected exception
                     * occurred creating the request or processing the response.
                     */
                    override fun onFailure(call: Call<UserProfileResponse>, error: Throwable) {
                        onUserProfileReceived(null, error)
                    }

                })
        }
    }
}