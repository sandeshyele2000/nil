package com.sandesh.nil.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sandesh.nil.core.NIL
import com.sandesh.nil.ui.theme.NILTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NIL.initialize(
            context = applicationContext,
            enableFloatingButton = true
        )

        setContent {
            NILTheme {
                SampleHostScreen()
            }
        }
    }
}

@Composable
private fun SampleHostScreen() {
    val scope = rememberCoroutineScope()
    val client = remember {
        OkHttpClient.Builder()
            .addInterceptor(NIL.interceptor())
            .build()
    }
    val retrofitApi = remember {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SampleApi::class.java)
    }

    var statusText by remember { mutableStateOf("No calls yet") }



    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
        Text("NIL Sample App", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                statusText = runRetrofitCall(retrofitApi)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Api,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retrofit Call")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            statusText = runOkHttpCall(client)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Http,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OkHttp Call")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        NIL.addMockEvent()
                        statusText = "Mock event added"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DataObject,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mock Event")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(statusText, style = MaterialTheme.typography.bodyMedium)
    }
}

private suspend fun runRetrofitCall(api: SampleApi): String {
    return withContext(Dispatchers.IO) {
        runCatching {
            val response: Response<Post> = api.getPost()
            "Retrofit: HTTP ${response.code()} ${response.body()?.title.orEmpty()}"
        }.getOrElse { throwable ->
            "Retrofit failed: ${throwable.message}"
        }
    }
}

private suspend fun runOkHttpCall(client: OkHttpClient): String {
    return withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://httpbin.org/get?from=okhttp")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                "OkHttp: HTTP ${response.code}"
            }
        }.getOrElse { throwable ->
            "OkHttp failed: ${throwable.message}"
        }
    }
}

private interface SampleApi {
    @GET("posts/1")
    suspend fun getPost(): Response<Post>
}

private data class Post(
    val id: Int,
    val title: String
)
