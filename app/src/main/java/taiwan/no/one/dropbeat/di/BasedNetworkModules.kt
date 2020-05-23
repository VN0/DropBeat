/*
 * MIT License
 *
 * Copyright (c) 2020 Jieyi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package taiwan.no.one.dropbeat.di

import android.content.Context
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import taiwan.no.one.core.data.remote.interceptor.ConnectInterceptor
import taiwan.no.one.dropbeat.di.Constant.TAG_BASE_OKHTTP
import java.util.concurrent.TimeUnit

object BasedNetworkModules {
    private const val TIME_OUT = 10L
    private const val CACHE_MAX_SIZE = 10 * 1024 * 1024L

    fun netProvider(context: Context) = Kodein.Module("NetworkModule") {
        bind<Converter.Factory>() with singleton { GsonConverterFactory.create(instance()) }
        bind<Cache>() with provider { Cache(context.cacheDir, CACHE_MAX_SIZE /* 10 MiB */) }
        // Build OkHttp object
        bind<OkHttpClient.Builder>(TAG_BASE_OKHTTP) with provider {
            OkHttpClient.Builder()
                .cache(instance())
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                // Keep the internet result into the cache.
                .apply {
                    addInterceptor(ConnectInterceptor(context))
                    // Those three are for HTTPS protocol.
                    connectionSpecs(mutableListOf(ConnectionSpec.RESTRICTED_TLS,
                                                  ConnectionSpec.MODERN_TLS,
                                                  ConnectionSpec.COMPATIBLE_TLS,
                        // This is for HTTP protocol.
                                                  ConnectionSpec.CLEARTEXT))
                }
        }
    }
}