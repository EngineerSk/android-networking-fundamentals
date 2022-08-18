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
import com.raywenderlich.android.taskie.model.response.GetTasksResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.ResponseCache
import java.net.URL
import java.nio.Buffer

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi {

    fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
        Thread {
            val connection = URL("$BASE_URL/api/login").openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                readTimeout = 10000
                connectTimeout = 10000
                doInput = true
                doOutput = true
            }
            val requestObject = JSONObject().apply {
                put("name", userDataRequest.name)
                put("email", userDataRequest.email)
                put("password", userDataRequest.password)
            }

            val bytes = requestObject.toString().toByteArray()
            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(bytes)
                }
                val reader = InputStreamReader(connection.inputStream)
                reader.use { inputStreamReader ->
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(inputStreamReader)
                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                        val jsonObject = JSONObject(response.toString())
                        val token = jsonObject.getString("token")
                        onUserLoggedIn(token, null)
                    }
                }
            } catch (error: Throwable) {
                onUserLoggedIn(null, error)
            }
            connection.disconnect()
        }.start()
    }

    fun registerUser(
        userDataRequest: UserDataRequest,
        onUserCreated: (String?, Throwable?) -> Unit
    ) {
        Thread {
            val connection = URL("$BASE_URL/api/register").openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                readTimeout = 10000
                connectTimeout = 10000
                doInput = true
                doOutput = true
            }
            val requestObject = JSONObject().apply {
                put("name", userDataRequest.name)
                put("email", userDataRequest.email)
                put("password", userDataRequest.password)
            }

            val bytes = requestObject.toString().toByteArray()

            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(bytes)
                }
                val reader = InputStreamReader(connection.inputStream)
                reader.use { inputStreamReader ->
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(inputStreamReader)
                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                        onUserCreated(JSONObject(response.toString()).getString("message"), null)
                    }
                }
            } catch (error: Throwable) {
                onUserCreated(null, error)
            }
            connection.disconnect()
        }.start()
    }

    fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
        val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            readTimeout = 10000
            connectTimeout = 10000
            doInput = true
            doOutput = true
        }

        try {
            val response = StringBuilder()
            connection.inputStream.buffered().reader().useLines { lines ->
                lines.forEach {
                    response.append(it.trim())
                }
                onTasksReceived(
                    App.gson.fromJson(
                        response.toString(),
                        GetTasksResponse::class.java
                    ).notes.filter { !it.isCompleted }, null
                )
            }

        } catch (error: Throwable) {
            onTasksReceived(emptyList(), error)
        }
        connection.disconnect()
    }

    fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
        onTaskDeleted(null)
    }

    fun completeTask(taskId: String, onTaskCompleted: (Throwable?) -> Unit) {
        Thread {
            val connection =
                URL("$BASE_URL/api/note/complete?id=$taskId").openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", App.getToken())
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                readTimeout = 10000
                connectTimeout = 10000
                doInput = true
                doOutput = true
            }
            try {
                val response = StringBuilder()
                connection.inputStream.buffered().reader().useLines { lines ->
                    lines.forEach {
                        response.append(it.trim())
                    }
                    onTaskCompleted(null)
                }
            } catch (error: Throwable) {
                onTaskCompleted(error)
            }
        }.start()
    }

    fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {
        Thread {
            val connection = URL("$BASE_URL/api/note").openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", App.getToken())
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                readTimeout = 10000
                connectTimeout = 10000
                doInput = true
                doOutput = true
            }
            val requestBody = JSONObject().apply {
                put("title", addTaskRequest.title)
                put("content", addTaskRequest.content)
                put("taskPriority", addTaskRequest.taskPriority)
            }
            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(requestBody.toString().toByteArray())
                }
                connection.inputStream.buffered().use { inputStream ->
                    val response = StringBuilder()
                    inputStream.reader().useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                        val jsonObject = JSONObject(response.toString())
                        onTaskCreated(
                            Task(
                                id = jsonObject.getString("id"),
                                title = jsonObject.getString("title"),
                                content = jsonObject.getString("content"),
                                isCompleted = jsonObject.getBoolean("isCompleted"),
                                taskPriority = jsonObject.getInt("taskPriority")
                            ), null
                        )
                    }
                }
            } catch (error: Throwable) {
                onTaskCreated(null, error)
            }
            connection.disconnect()
        }.start()
    }

    fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
        onUserProfileReceived(UserProfile("mail@mail.com", "Filip", 10), null)
    }
}