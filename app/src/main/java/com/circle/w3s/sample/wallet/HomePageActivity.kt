package com.circle.w3s.sample.wallet

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.circle.w3s.sample.wallet.databinding.HomepageBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.circle.w3s.sample.wallet.ui.main.LoadingDialog

data class GetUserWalletResponse(
    val data: WalletData
)

data class WalletData(
    val wallets: List<Wallet>
)

data class Wallet(
    val id: String,
    val state: String,
    val walletSetId: String,
    val custodyType: String,
    val userId: String,
    val address: String,
    val blockchain: String,
    val accountType: String,
    val updateDate: String,
    val createDate: String
)

data class TokenBalanceResponse(
    val data: TokenBalanceData
)

data class TokenBalanceData(
    val tokenBalances: List<TokenBalance>
)

data class TokenBalance(
    val token: TokenInfo,
    val amount: String,
    val updateDate: String
)

data class TokenInfo(
    val id: String,
    val blockchain: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val isNative: Boolean,
    val updateDate: String,
    val createDate: String
)

class HomePageActivity : AppCompatActivity() {

    // Values to retrieve from the API
    private var userId = ""
    private var userWalletId = ""
    private var userWalletAddress = ""

    private var avaxTokenId = "87a5c41c-fcb4-5973-8c66-72e2ed851ab8"
    private var avaxTokenSymbol = "AVAX-FUJI"
    private var userAvaxTokenBalance = "0"

    private var usdcTokenId = "ff47a560-9795-5b7c-adfc-8f47dad9e06a"
    private var usdcTokenSymbol = "USDC"
    private var userUSDCTokenBalance = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = HomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val loadingDialog = LoadingDialog(this@HomePageActivity, "Getting wallet data, please wait...") // Specify the loading text here

        // Initialize UI components
        val statusLoadingTextView = binding.statusLoadingTextView
        val avaxTokenBalanceText = binding.avaxTokenBalanceText
        val usdcTokenBalanceText = binding.usdcTokenBalanceText
        val refreshButton = binding.refreshbutton
        val receiveButton = binding.receiveBtn
        val sendButton = binding.SendBtn
        val viewTransactions = binding.viewTransactionBtn
        val logoutButton = binding.logoutButton
        val copyButton = binding.copyButton
        copyButton.visibility = View.INVISIBLE
        val walletAddressText = binding.walletAddressTextView
        walletAddressText.visibility = View.INVISIBLE

        // Retrieve values from the intent extras
        val apiKey = intent.getStringExtra("apiKey")
        val userToken = intent.getStringExtra("userToken")
        val encryptionKey = intent.getStringExtra("encryptionKey")
        val appId = intent.getStringExtra("appId")

        // Delay before making network requests (5 seconds)
        val delayMilliseconds = 2500L
        loadingDialog.show()
        // Start the network request to get user wallet id
        getUserWalletId(apiKey, userToken, statusLoadingTextView, avaxTokenBalanceText, usdcTokenBalanceText, delayMilliseconds, walletAddressText, copyButton, loadingDialog)

        sendButton.setOnClickListener{
            Log.d("HomePageActivity", "On Send button press")

            //redirect to send tokens page
            val intent = Intent(this@HomePageActivity, SendTokenActivity::class.java)

            //pass data to next page
            intent.putExtra("apiKey", apiKey)
            intent.putExtra("userToken", userToken)
            intent.putExtra("encryptionKey", encryptionKey)
            intent.putExtra("walletId", userWalletId)
            intent.putExtra("avaxTokenId", avaxTokenId)
            intent.putExtra("avaxTokenBalance", userAvaxTokenBalance)
            intent.putExtra("usdcTokenId", usdcTokenId)
            intent.putExtra("usdcTokenBalance", userUSDCTokenBalance)
            intent.putExtra("appId", appId)

            // Start the new activity
            startActivity(intent)

            // Finish the current activity if needed
            finish()

        }

        logoutButton.setOnClickListener {
            //redirect to view home page
            val intent = Intent(this@HomePageActivity, WalletCreationActivity::class.java)
            // Start the new activity
            startActivity(intent)

            // Finish the current activity if needed
            finish()
        }

        viewTransactions.setOnClickListener {
            Log.d("HomePageActivity", "On view transactions button press")

            //redirect to view transactions page
            val intent = Intent(this@HomePageActivity, TransactionsActivity::class.java)
            //pass data to next page
            intent.putExtra("apiKey", apiKey)
            intent.putExtra("userToken", userToken)
            intent.putExtra("encryptionKey", encryptionKey)
            intent.putExtra("userId", userId)
            intent.putExtra("appId", appId)

            // Start the new activity
            startActivity(intent)

            // Finish the current activity if needed
            finish()
        }

        copyButton.setOnClickListener {
            // Get the text you want to copy
            val textToCopy = userWalletAddress
            // Get the clipboard manager
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Create a ClipData object to hold the text
            val clipData = ClipData.newPlainText("label", textToCopy)

            // Set the data to clipboard
            clipboardManager.setPrimaryClip(clipData)

            // Show a toast to confirm the copy operation
            Toast.makeText(this, "Copied to clipboard: $textToCopy", Toast.LENGTH_SHORT).show()
        }

        //receive button click
        receiveButton.setOnClickListener {
            Toast.makeText(this@HomePageActivity, "Click on copy button to copy your wallet address.", Toast.LENGTH_LONG).show()
            walletAddressText.visibility = View.VISIBLE
            copyButton.visibility = View.VISIBLE

            walletAddressText.text = "Wallet Address: $userWalletAddress"

        }

        //refresh button click
        refreshButton.setOnClickListener{
            Log.d("HomePageActivity", "On Refresh button press")
            statusLoadingTextView.text = "Loading....Getting wallet data"
            loadingDialog.show()
            // Delay before making network requests (2 seconds)
            val delayInMilliseconds = 2000L
            if (userWalletId.isNotEmpty()){
                getUserWalletId(apiKey, userToken, statusLoadingTextView, avaxTokenBalanceText, usdcTokenBalanceText, delayInMilliseconds, walletAddressText, copyButton, loadingDialog)
            }
        }

    }

    private fun getUserTokenBalance(
        apiKey: String?,
        userToken: String?,
        avaxTokenBalanceText: TextView,
        usdcTokenBalanceText: TextView,
        statusLoadingTextView: TextView,
        walletAddressText: TextView,
        copyButton: Button,
        loadingDialog: LoadingDialog,
    ) {
        Log.d("HomePageActivity", "Getting Token Balance: $userToken, $userWalletId")
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.circle.com/v1/w3s/wallets/$userWalletId/balances?pageSize=10")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("X-User-Token", "$userToken")
                .addHeader("authorization", "Bearer $apiKey")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Use Gson to parse the JSON response into your data class
                    val gson = Gson()
                    val responseObject = gson.fromJson(responseBody, TokenBalanceResponse::class.java)
                    val tokenBalanceArrayData = responseObject.data.tokenBalances
                    //NOTE: when creating wallet for first time, user does not have any token balance
                    //calling the get token balance endpoint will return empty array, only when
                    //user wallet has some balance tokens will the data be returned.
                    Log.d("HomePageActivity", "Token Balances data: $tokenBalanceArrayData")

                    //filter to get user token balance (USDC and AVAX-FUJI)
                    val avaxTestnetTokenData = tokenBalanceArrayData.filter {
                        token -> token.token.name == "Avalanche-Fuji"
                    }

                    val usdcTokenData = tokenBalanceArrayData.filter {
                            token -> token.token.name == "USD Coin"
                    }

                    if (tokenBalanceArrayData.isNotEmpty()) {
                        if(avaxTestnetTokenData.isNotEmpty()){
                            val avaxWalletDetails = avaxTestnetTokenData[0]
                            userAvaxTokenBalance = avaxWalletDetails.amount
                            avaxTokenSymbol = avaxWalletDetails.token.symbol
                            avaxTokenId = avaxWalletDetails.token.id
                        }

                        if(usdcTokenData.isNotEmpty()){
                            val usdcWalletDetail = usdcTokenData[0]
                            userUSDCTokenBalance = usdcWalletDetail.amount
                            usdcTokenSymbol = usdcWalletDetail.token.symbol
                            usdcTokenId = usdcWalletDetail.token.id
                        }

                        // Update UI components
                        runOnUiThread {
                            statusLoadingTextView.text = "Success! You can now proceed to send/receive or view past transactions."
                            if (avaxTestnetTokenData.isNotEmpty()) {
                                avaxTokenBalanceText.text = " $avaxTokenSymbol: $userAvaxTokenBalance"
                            }
                            if(usdcTokenData.isNotEmpty()){
                                usdcTokenBalanceText.text = " $usdcTokenSymbol: $userUSDCTokenBalance"
                            }
                            loadingDialog.dismiss()
                            walletAddressText.visibility = View.VISIBLE
                            copyButton.visibility = View.VISIBLE

                            walletAddressText.text = "Wallet Address: $userWalletAddress"
                        }
                    } else {
                        // Update UI components
                        runOnUiThread {
                            statusLoadingTextView.text = "Success! You have no tokens in your wallet, send some AVAX-Fuji tokens to your wallet address."
                            loadingDialog.dismiss()
                            walletAddressText.visibility = View.VISIBLE
                            copyButton.visibility = View.VISIBLE

                            walletAddressText.text = "Wallet Address: $userWalletAddress"
                        }
                        Log.d("HomePageActivity", "No Token Balances data found")
                    }
                } else {
                    // Handle API response error
                    Log.e("HomePageActivity", "Error ${response.code}")
                    runOnUiThread {
                        loadingDialog.dismiss()
                    }
                }
            } catch (e: IOException) {
                Log.e("HomePageActivity", "Get Wallets Error: ${e.message}", e)
            }
        }
    }

    private fun getUserWalletId(
        apiKey: String?,
        userToken: String?,
        statusLoadingTextView: TextView,
        avaxTokenBalanceText: TextView,
        usdcTokenBalanceText: TextView,
        delayMilliseconds: Long,
        walletAddressText: TextView,
        copyButton: Button,
        loadingDialog: LoadingDialog,
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(delayMilliseconds)

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://api.circle.com/v1/w3s/wallets")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("X-User-Token", "$userToken")
                .addHeader("authorization", "Bearer $apiKey")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Use Gson to parse the JSON response into your data class
                    val gson = Gson()
                    val responseObject = gson.fromJson(responseBody, GetUserWalletResponse::class.java)

                    val myWalletObjectArray = responseObject.data.wallets

                    if (myWalletObjectArray.isNotEmpty()) {
                        val firstWallet = myWalletObjectArray[0]
                        userWalletId = firstWallet.id
                        userWalletAddress = firstWallet.address
                        userId = firstWallet.userId

                        // Call the function to get user token balance
                        getUserTokenBalance(apiKey, userToken, avaxTokenBalanceText, usdcTokenBalanceText, statusLoadingTextView, walletAddressText, copyButton, loadingDialog)
                    } else {
                        // Handle the case when the array is empty
                        Log.e("HomePageActivity", "No Wallets found for user.")
                    }
                } else {
                    // Handle error response
                    Log.e("HomePageActivity", "Error ${response.code}")
                    runOnUiThread {
                        loadingDialog.dismiss()
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    loadingDialog.dismiss()
                    Log.e("HomePageActivity", "Get Wallets Error: ${e.message}", e)
                }
            }
        }
    }
}
