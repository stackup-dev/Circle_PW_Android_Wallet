package com.circle.w3s.sample.wallet

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.circle.w3s.sample.wallet.databinding.AcquireTokenForExistingUserBinding
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.Gson
import android.content.Intent

data class UserSessionTokenResponse(
    val data: UserData
)

data class UserSessionData(
    val userToken: String,
    val encryptionKey: String
)

class AcquireSessionTokenExistingUser: AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = AcquireTokenForExistingUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tokenResponseText = binding.acquireTokenText2
        val loadingProgressBar = binding.loadingProgressBar2

        tokenResponseText.visibility = android.view.View.INVISIBLE
        loadingProgressBar.visibility = android.view.View.VISIBLE

        // Retrieve apiKey and userId from the intent extras
        val apiKey = intent.getStringExtra("apiKey")
        val userId = intent.getStringExtra("userId")
        val appId = intent.getStringExtra("appId")

        Log.d("AcquireSessionTokenExistingUserActivity", "Msg: ${apiKey}, $userId")
        //api call

        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val mediaType = "application/json".toMediaTypeOrNull()
            val body = "{\"userId\":\"$userId\"}".toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://api.circle.com/v1/w3s/users/token")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer $apiKey")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Use Gson to parse the JSON response into your data class
                    val gson = Gson()
                    val responseObject = gson.fromJson(responseBody,UserSessionTokenResponse::class.java)

                    // Now you can access the parsed data
                    val userToken = responseObject.data.userToken
                    val encryptionKey = responseObject.data.encryptionKey
                    Log.d("AcquireSessionTokenExistingUserActivity", "parseData: $userToken, $encryptionKey")

                    runOnUiThread {
                        tokenResponseText.visibility = android.view.View.VISIBLE
                        tokenResponseText.text = "Successfully Acquired session token."

                        val intent = Intent(this@AcquireSessionTokenExistingUser, HomePageActivity::class.java)

                        //pass data to next page
                        intent.putExtra("apiKey", apiKey)
                        intent.putExtra("userToken", userToken)
                        intent.putExtra("encryptionKey", encryptionKey)
                        intent.putExtra("appId", appId)

                        // Start the new activity
                        startActivity(intent)

                        // Finish the current activity if needed
                        finish()
                    }


                } else {
                    // Handle error response
                    Log.e("AcquireSessionTokenExistingUserActivity", "Error: ${response}")
                    loadingProgressBar.visibility = android.view.View.INVISIBLE
                }
            } catch (e: IOException) {
                Log.e("AcquireSessionTokenExistingUserActivity", "Error: ${e.message}", e)

            }
        }

    }

}