package com.benmohammad.mobiuselm.data

import io.reactivex.Single
import org.eclipse.egit.github.core.Repository

interface IApiService {

    fun getUserName(): String

    fun login(login: String, pass: String): Single<Boolean>

    fun getStarredRepos(): Single<List<Repository>>
}