package com.raywenderlich.android.taskie.model.response

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Holds a message response after a note is completed.
 */
@Serializable
class CompleteNoteResponse(@SerialName(value = "message") val message: String?)