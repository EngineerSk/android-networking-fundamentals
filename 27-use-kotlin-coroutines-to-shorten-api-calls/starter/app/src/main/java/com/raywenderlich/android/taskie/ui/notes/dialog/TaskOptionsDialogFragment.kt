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

package com.raywenderlich.android.taskie.ui.notes.dialog

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.raywenderlich.android.taskie.App
import com.raywenderlich.android.taskie.R
import com.raywenderlich.android.taskie.model.Failure
import com.raywenderlich.android.taskie.model.Success
import com.raywenderlich.android.taskie.networking.NetworkStatusChecker
import kotlinx.android.synthetic.main.fragment_dialog_task_options.*
import kotlinx.coroutines.*

/**
 * Displays the options to delete or complete a task.
 */
class TaskOptionsDialogFragment : DialogFragment() {

    private var taskOptionSelectedListener: TaskOptionSelectedListener? = null

    private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
        NetworkStatusChecker(activity?.getSystemService(ConnectivityManager::class.java))
    }

    companion object {
        private const val KEY_TASK_ID = "task_id"

        fun newInstance(taskId: String): TaskOptionsDialogFragment =
            TaskOptionsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TASK_ID, taskId)
                }
            }
    }

    interface TaskOptionSelectedListener {
        fun onTaskDeleted(taskId: String)

        fun onTaskCompleted(taskId: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FragmentDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_task_options, container)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initUi() {
        val taskId = arguments?.getString(KEY_TASK_ID) ?: ""
        if (taskId.isEmpty()) dismissAllowingStateLoss()

        deleteTask.setOnClickListener {
            networkStatusChecker.performIfConnectedToInternet {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = remoteApi.deleteTask(taskId)
                    if (result is Success) {
                        taskOptionSelectedListener?.onTaskDeleted(taskId)
                    }
                    dismissAllowingStateLoss()
                }

            }
        }

        completeTask.setOnClickListener {
            networkStatusChecker.performIfConnectedToInternet {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = remoteApi.completeTask(taskId)
                    if(result is Success){
                        taskOptionSelectedListener?.onTaskCompleted(taskId)
                    }
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    fun setTaskOptionSelectedListener(taskOptionSelectedListener: TaskOptionSelectedListener) {
        this.taskOptionSelectedListener = taskOptionSelectedListener
    }
}