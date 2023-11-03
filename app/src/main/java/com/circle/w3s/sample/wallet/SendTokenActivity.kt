package com.circle.w3s.sample.wallet

import android.R
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.circle.w3s.sample.wallet.databinding.SendpageBinding
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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import java.util.UUID
import com.circle.w3s.sample.wallet.ui.main.LoadingDialog

data class ChallengeResponse(
    val data: ChallengeData
)

data class ChallengeData(
    val challengeId: String
)


class SendTokenActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding = SendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipientWalletAddressUserInput = binding.recipientInputEditText
        val tokenAmountUserInput = binding.tokenAmountEditText
        val sendButton = binding.sendTransferButton
        val backButton = binding.backButton

        val dropdownlist = binding.tokenSpinner
        var selectedToken = "AVAX-FUJI"

        // Define an array of items to populate the Spinner
        val items = arrayOf("AVAX-FUJI", "USDC")

        // Create an ArrayAdapter using the array of items and a default spinner layout
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapter to the Spinner
        dropdownlist.adapter = adapter

        // Retrieve apiKey and userId from the intent extras
        val apiKey = intent.getStringExtra("apiKey")
        val userToken = intent.getStringExtra("userToken")
        val encryptionKey = intent.getStringExtra("encryptionKey")
        val walletId = intent.getStringExtra("walletId")
        val avaxTokenId = intent.getStringExtra("avaxTokenId")
        val avaxTokenBalance = intent.getStringExtra("avaxTokenBalance")
        val usdcTokenId = intent.getStringExtra("usdcTokenId")
        val usdcTokenBalance = intent.getStringExtra("usdcTokenBalance")
        val appId = intent.getStringExtra("appId")


        // Set up the OnItemSelectedListener
        dropdownlist.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = items[position] // Get the selected item
                selectedToken = selectedItem
                Log.d("SendTokenActivity", "$selectedToken")
                // Do something with the selected item, e.g., display it or store it.
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }

        backButton.setOnClickListener {
            //redirect to send tokens page
            val intent = Intent(this@SendTokenActivity, HomePageActivity::class.java)

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

        sendButton.setOnClickListener {
            Log.d("SendTokenActivity", "On Send button click.")
            recipientWalletAddressUserInput.error = null
            tokenAmountUserInput.error = null

            val avaxTokenBalance = avaxTokenBalance?.toDouble()
            val usdcTokenBalance = usdcTokenBalance?.toDouble()

            //validate input fields
            val recipientWalletAddress = recipientWalletAddressUserInput.text.toString().trim()
            if (recipientWalletAddress.isEmpty()) {
                // userId is empty, display a warning message
                recipientWalletAddressUserInput.error = "Recipient wallet address is required."
            }

            val tokenAmountInput = tokenAmountUserInput.text.toString().trim()
            if (tokenAmountInput.isEmpty()) {
                // userId is empty, display a warning message
                tokenAmountUserInput.error = "Token amount is required."
            }

            val userInputDecimal = tokenAmountUserInput.text.toString().trim().toDouble()
            Log.d("SendTokenActivity", "$selectedToken")
            if(selectedToken == "AVAX-FUJI"){
                if(userInputDecimal > avaxTokenBalance!! * 0.9){
                    tokenAmountUserInput.error = "Token amount cannot be more than wallet token balance. Need to account for gas fees as well."
                }
            }

            if(selectedToken == "USDC"){
                if(userInputDecimal >= usdcTokenBalance!!){
                    tokenAmountUserInput.error = "Token amount cannot be more than wallet token balance."
                }
            }

            if(recipientWalletAddressUserInput.error == null && tokenAmountUserInput.error == null){
                val uuid = UUID.randomUUID()
                //show loading modal
                val loadingDialog = LoadingDialog(this, "Processing your request, please wait...") // Specify the loading text here
                loadingDialog.show()

                Log.d("SendTokenActivity", "NEW BODY TEST")

                val apiCallTokenId = if (selectedToken == "AVAX-FUJI") {
                    avaxTokenId
                } else {
                    usdcTokenId
                }

                //api call
                GlobalScope.launch(Dispatchers.IO) {
                    //Step 6 - PASTE CODE HERE FOR "SEND TOKEN" API


                    try {
                        val response = client.newCall(request).execute()

                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            // Use Gson to parse the JSON response into your data class
                            val gson = Gson()
                            val responseObject = gson.fromJson(responseBody,ChallengeResponse::class.java)

                            val challengeIdResponse = responseObject.data.challengeId
                            Log.e("SendTokenActivity", "Data: $challengeIdResponse")
                            runOnUiThread {
                                loadingDialog.dismiss()
                                //redirect to main fragment

                                val intent = Intent(this@SendTokenActivity, MainActivity::class.java)

                                //pass data to next page, user only needs to input APP ID
                                intent.putExtra("apiKey", apiKey)
                                intent.putExtra("userToken", userToken)
                                intent.putExtra("encryptionKey", encryptionKey)
                                intent.putExtra("challengeId", challengeIdResponse)
                                intent.putExtra("appId", appId)

                                // Start the new activity
                                startActivity(intent)

                                // Finish the current activity if needed
                                finish()
                            }

                        } else {
                            // Update UI components
                            runOnUiThread {
                                Log.e("SendTokenActivity", "Error: $response")
                                loadingDialog.dismiss()
                                //show toast message
                                Toast.makeText(this@SendTokenActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                                 }

                            }


                    }  catch (e: IOException) {
                        Log.e("SendTokenActivity", "Error: ${e.message}", e)
                    }

                }
            }

        }

    }
}