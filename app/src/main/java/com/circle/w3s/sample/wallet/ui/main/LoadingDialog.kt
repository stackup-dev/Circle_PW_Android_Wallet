package com.circle.w3s.sample.wallet.ui.main
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.circle.w3s.sample.wallet.R

class LoadingDialog(context: Context) : Dialog(context) {

    private var loadingText: String = ""

    // Initialize the loading dialog with a custom loading text
    constructor(context: Context, text: String) : this(context) {
        loadingText = text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog_layout) // Create a custom layout for the loading dialog

        val loadingTextView = findViewById<TextView>(R.id.loadingTextView)
        loadingTextView.text = loadingText // Set the loading text dynamically

        // Prevent the dialog from being canceled by touching outside
        setCanceledOnTouchOutside(false)
    }
}
