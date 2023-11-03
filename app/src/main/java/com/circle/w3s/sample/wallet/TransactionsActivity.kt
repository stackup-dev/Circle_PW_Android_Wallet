package com.circle.w3s.sample.wallet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.circle.w3s.sample.wallet.databinding.TransactionspageBinding
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.Gson
import android.content.Intent
import android.graphics.Color
import android.text.Html
import android.view.View
import com.circle.w3s.sample.wallet.ui.main.LoadingDialog
import android.widget.TableRow
import android.widget.TextView
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


data class TransactionData(
    val data: TransactionList
)

data class TransactionList(
    val transactions: List<Transaction>
)

data class Transaction(
    val id: String,
    val blockchain: String,
    val tokenId: String,
    val walletId: String,
    val sourceAddress: String,
    val destinationAddress: String,
    val transactionType: String,
    val custodyType: String,
    val state: String,
    val amounts: List<String>,
    val nfts: Any?, // Change to the appropriate type if needed
    val txHash: String,
    val blockHash: String,
    val blockHeight: Long,
    val networkFee: String,
    val firstConfirmDate: String,
    val operation: String,
    val userId: String,
    val abiParameters: Any?, // Change to the appropriate type if needed
    val createDate: String,
    val updateDate: String
)

// Define a data class to hold the selected fields
data class TransactionInfo(
    val sourceAddress: String,
    val destinationAddress: String,
    val state: String,
    val amount: String,
    val txHash: String,
    val userId: String,
    val transactionType: String,
    val blockchain: String,
    val walletId: String,
    val createDate: String,
    val updateDate: String,
    val tokenId: String,
)

class TransactionsActivity: AppCompatActivity()  {
    private var transactions: List<TransactionInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = TransactionspageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //button and table layouts
        val backButton = binding.backBtn
        val backToAllTransactionsBtn = binding.backToTransactionsTableButton
        backToAllTransactionsBtn.visibility = View.INVISIBLE
        val tableLayout = binding.tableLayout

        //Scroll views
        val transactionTableScrollView = binding.transactionTablescrollView
        transactionTableScrollView.visibility = View.VISIBLE
        val transactionDetailsScrollView = binding.transactionDetailsScrollView
        transactionDetailsScrollView.visibility = View.INVISIBLE

        //TextViews for displaying transaction details
        val sourceAddressTextView  = binding.sourceAddressTextView
        val destinationAddressTextView = binding.destinationAddressTextView
        val stateText = binding.stateTextView
        val amountText = binding.amountTextView
        val txHashTextView = binding.txHashTextView
        val transactionTypeText = binding.transactionTypeTextView
        val tokenTextView = binding.tokenTextView
        val blockchainText = binding.blockchainTextView
        val walletIdText = binding.walletIdTextView
        val createDateText = binding.createDateTextView
        val updateDateText = binding.updateDateTextView

        // Retrieve apiKey and userId from the intent extras
        val apiKey = intent.getStringExtra("apiKey")
        val userToken = intent.getStringExtra("userToken")
        val encryptionKey = intent.getStringExtra("encryptionKey")
        val userId = intent.getStringExtra("userId")
        val appId = intent.getStringExtra("appId")
        val loadingDialog = LoadingDialog(this@TransactionsActivity, "Getting past transaction data, please wait...") // Specify the loading text here
        loadingDialog.show()

        //call API to get transaction data
        GlobalScope.launch(Dispatchers.IO) {
            //Step 7 - PASTE CODE HERE FOR "FETCH TRANSACTIONS" API

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Use Gson to parse the JSON response into your data class
                    val gson = Gson()
                    val responseObject = gson.fromJson(responseBody, TransactionData::class.java)

                    val transactionsResponseData = responseObject.data.transactions
                    // Map the transactions to a new list with selected fields
                    val selectedFieldsList = transactionsResponseData.map { transaction ->
                        // Create a new object with the selected fields
                        TransactionInfo(
                            transaction.sourceAddress,
                            transaction.destinationAddress,
                            transaction.state,
                            transaction.amounts.firstOrNull() ?: "0", // Handle the case when amounts is empty
                            transaction.txHash,
                            transaction.userId,
                            transaction.transactionType,
                            transaction.blockchain,
                            transaction.walletId,
                            transaction.createDate,
                            transaction.updateDate,
                            transaction.tokenId,
                        )
                    }
                    transactions = selectedFieldsList
                    Log.d("TransactionsActivity", "Data response: $selectedFieldsList")
                    // Update the UI on the main thread
                    runOnUiThread {
                        for (transaction in transactions) {

                            val tokenName = if (transaction.tokenId == "87a5c41c-fcb4-5973-8c66-72e2ed851ab8") {
                                "Avalanche-Fuji"
                            } else {
                                "USD Coin"
                            }

                            val tableRow = TableRow(this@TransactionsActivity)
                            val tokenNameTextView = TextView(this@TransactionsActivity)
                            tokenNameTextView.text = tokenName
                            tokenNameTextView.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT
                            )
                            tokenNameTextView.setPadding(15, 15, 15, 15) // Set padding to 15
                            tokenNameTextView.setBackgroundResource(R.drawable.table_border)
                            tokenNameTextView.setTextColor(Color.BLACK)
                            tableRow.addView(tokenNameTextView)

                            val amountTextView = TextView(this@TransactionsActivity)
                            amountTextView.text = transaction.amount
                            amountTextView.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT
                            )
                            amountTextView.setPadding(15, 15, 15, 15) // Set padding to 15
                            amountTextView.setBackgroundResource(R.drawable.table_border)
                            amountTextView.setTextColor(Color.BLACK)
                            tableRow.addView(amountTextView)

                            val transactionTypeTextView = TextView(this@TransactionsActivity)
                            transactionTypeTextView.text = transaction.transactionType
                            transactionTypeTextView.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.MATCH_PARENT
                            )
                            transactionTypeTextView.setPadding(15, 15, 15, 15) // Set padding to 15
                            transactionTypeTextView.setBackgroundResource(R.drawable.table_border)
                            transactionTypeTextView.setTextColor(Color.BLACK)
                            tableRow.addView(transactionTypeTextView)

                            val viewDetailsBtn = Button(this@TransactionsActivity)
                            viewDetailsBtn.text = "View details"
                            viewDetailsBtn.textSize = 10f
                            viewDetailsBtn.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                            )
                            viewDetailsBtn.setPadding(15, 15, 15, 15)
                            viewDetailsBtn.setTextColor(Color.BLACK)
                            tableRow.addView(viewDetailsBtn)

                            viewDetailsBtn.setOnClickListener {
                                //set visibility of views
                                transactionTableScrollView.visibility = View.INVISIBLE
                                backButton.visibility = View.INVISIBLE
                                transactionDetailsScrollView.visibility = View.VISIBLE
                                backToAllTransactionsBtn.visibility = View.VISIBLE

                                //set text of textview
                                val sourceAddressTextHtml = "<b>Source Address:</b> ${transaction.sourceAddress}"
                                sourceAddressTextView.text = Html.fromHtml(sourceAddressTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val destinationAddressTextHtml = "<b>Destination Address:</b> ${transaction.destinationAddress}"
                                destinationAddressTextView.text = Html.fromHtml(destinationAddressTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val stateTextHtml = "<b>State:</b> ${transaction.state}"
                                stateText.text = Html.fromHtml(stateTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val amountTextHtml = "<b>Amount:</b> ${transaction.amount}"
                                amountText.text = Html.fromHtml(amountTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val txHashTextHtml = "<b>Transaction Hash:</b> ${transaction.txHash}"
                                txHashTextView.text = Html.fromHtml(txHashTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val transactionTypeTextHtml = "<b>Transaction Type:</b> ${transaction.transactionType}"
                                transactionTypeText.text = Html.fromHtml(transactionTypeTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val tokenName = if (transaction.tokenId == "87a5c41c-fcb4-5973-8c66-72e2ed851ab8") {
                                    "Avalanche-Fuji"
                                } else {
                                    "USD Coin"
                                }

                                val tokenIdTextHtml = "<b>Token:</b> $tokenName"
                                tokenTextView.text = Html.fromHtml(tokenIdTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val blockchainTextHtml = "<b>Blockchain:</b> ${transaction.blockchain}"
                                blockchainText.text = Html.fromHtml(blockchainTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val walletIdTextHtml = "<b>Wallet Id:</b> ${transaction.walletId}"
                                walletIdText.text = Html.fromHtml(walletIdTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                // Sample input strings
                                val createDate = transaction.createDate
                                val updateDate = transaction.updateDate

                                // Define the input format
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                                // Parse the create date and update date into Date objects
                                val createDateObject: Date = inputFormat.parse(createDate)
                                val updateDateObject: Date = inputFormat.parse(updateDate)

                                // Define the desired output format
                                val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                                outputFormat.timeZone = TimeZone.getDefault() // Use the device's time zone

                                // Format the Date objects into readable date-time strings
                                val formattedCreateDate = outputFormat.format(createDateObject)
                                val formattedUpdateDate = outputFormat.format(updateDateObject)

                                val createDateTextHtml = "<b>Created at:</b> $formattedCreateDate"
                                createDateText.text = Html.fromHtml(createDateTextHtml, Html.FROM_HTML_MODE_LEGACY)

                                val updateDateTextHtml = "<b>Updated at:</b> $formattedUpdateDate"
                                updateDateText.text = Html.fromHtml(updateDateTextHtml, Html.FROM_HTML_MODE_LEGACY)

                            }

                            tableLayout.addView(tableRow)
                        }

                        loadingDialog.dismiss()
                    }
                } else {
                    // Update UI components
                    runOnUiThread {
                        Log.e("TransactionsActivity", "Error: $response")
                        loadingDialog.dismiss()
                        //show toast message
                        Toast.makeText(this@TransactionsActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: IOException) {
                Log.e("TransactionsActivity", "Error: ${e.message}", e)
            }

        }

        backButton.setOnClickListener{
            //redirect to send tokens page
            val intent = Intent(this@TransactionsActivity, HomePageActivity::class.java)
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

        backToAllTransactionsBtn.setOnClickListener{
            transactionTableScrollView.visibility = View.VISIBLE
            transactionDetailsScrollView.visibility = View.INVISIBLE
            backToAllTransactionsBtn.visibility = View.INVISIBLE
            backButton.visibility = View.VISIBLE
        }

    }
}