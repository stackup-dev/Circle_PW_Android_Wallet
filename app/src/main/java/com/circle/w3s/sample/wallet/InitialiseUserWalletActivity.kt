package com.circle.w3s.sample.wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.circle.w3s.sample.wallet.databinding.InitialiseAccountBinding
import com.circle.w3s.sample.wallet.ui.main.MainFragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID


data class InitialiseUserResponse(
    val data: ChallengeIdData
)

data class ChallengeIdData(
    val challengeId: String
)
class InitialiseUserWalletActivity  : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = InitialiseAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var challengeIdValue = ""

        val loadingProgressBar = binding.createWalletProgressBar
        val nextBtn = binding.nextButton
        val statusText = binding.statusTextView

        nextBtn.visibility = android.view.View.INVISIBLE

        // Retrieve values from the intent extras
        val apiKey = intent.getStringExtra("apiKey")
        //val userId = intent.getStringExtra("userId")
        val userToken = intent.getStringExtra("userToken")
        val encryptionKey = intent.getStringExtra("encryptionKey")
        val appId = intent.getStringExtra("appId")
        val uuid = UUID.randomUUID()

        //call api to initialize the User's Account and Acquire the Challenge ID to create wallet
        GlobalScope.launch(Dispatchers.IO) {
            //Step 5 - PASTE CODE HERE FOR "CREATE WALLET" API


            try {
                val response = client.newCall(request).execute()
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()

                        // Use Gson to parse the JSON response into your data class
                        val gson = Gson()
                        val responseObject = gson.fromJson(responseBody, InitialiseUserResponse::class.java)

                        // Now you can access the parsed data
                        val challengeId = responseObject.data.challengeId
                        challengeIdValue = challengeId

                        Log.d("InitialiseUserWalletActivity", "ChallengeId: $challengeIdValue\n" +
                                "Encryption Key: $encryptionKey\n" +
                                " User Session Token: $userToken")
                        //change visibility of buttons, progress bar on success
                        nextBtn.visibility = android.view.View.VISIBLE
                        loadingProgressBar.visibility = android.view.View.INVISIBLE

                        // change header text
                        statusText.text = "SUCCESS! Click next to proceed to PIN setup."


                    } else {
                        // Handle error response
                        Log.e("InitialiseUserWalletActivity", "Error ${response.code}")
                        loadingProgressBar.visibility = android.view.View.INVISIBLE
                        loadingProgressBar.visibility = android.view.View.INVISIBLE
                        // change header text
                        statusText.text = "Process FAILED."
                    }
                }

            } catch (e: IOException) {
                Log.e("InitialiseUserWalletActivity", "Error: ${e.message}", e)
            }

            nextBtn.setOnClickListener {
                Log.d("InitialiseUserWalletActivity", "On Next press")
                //redirect to form page to input fields for challenge ID
                val intent = Intent(this@InitialiseUserWalletActivity, MainActivity::class.java)

                //pass data to next page, user only needs to input APP ID
                intent.putExtra("apiKey", apiKey)
                intent.putExtra("userToken", userToken)
                intent.putExtra("encryptionKey", encryptionKey)
                intent.putExtra("challengeId", challengeIdValue)
                intent.putExtra("appId", appId)

                // Start the new activity
                startActivity(intent)

                // Finish the current activity if needed
                finish()
            }
        }
    }



}