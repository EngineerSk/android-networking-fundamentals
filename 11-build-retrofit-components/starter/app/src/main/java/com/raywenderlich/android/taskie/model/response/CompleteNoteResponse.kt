package com.raywenderlich.android.taskie.model.response

import com.squareup.moshi.Json

data class CompleteNoteResponse(
    @field:Json(name = "message") val message: String?
)
