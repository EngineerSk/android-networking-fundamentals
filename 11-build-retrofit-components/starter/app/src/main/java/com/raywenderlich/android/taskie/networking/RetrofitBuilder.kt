package com.raywenderlich.android.taskie.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun buildClient(): OkHttpClient = OkHttpClient.Builder().build()

fun buildRetrofit(): Retrofit = Retrofit.Builder().client(buildClient()).baseUrl(BASE_URL).addConverterFactory(MoshiConverterFactory.create().asLenient()).build()

fun buildApiService(): RemoteApiService = buildRetrofit().create(RemoteApiService::class.java)