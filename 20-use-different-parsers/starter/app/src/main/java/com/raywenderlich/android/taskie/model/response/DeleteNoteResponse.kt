package com.raywenderlich.android.taskie.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteNoteResponse(@SerialName(value = "message") val message: String)
