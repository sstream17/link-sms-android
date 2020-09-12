package com.stream_suite.link.fragment.bottom_sheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.stream_suite.link.R

class CopyMessageTextFragment(val message: String) : TabletOptimizedBottomSheetDialogFragment() {

    constructor() : this("")

    override fun createLayout(inflater: LayoutInflater): View {
        val contentView = View.inflate(activity, R.layout.bottom_sheet_copy, null)

        val copyText = contentView.findViewById<View>(R.id.copy_all)
        copyText.setOnClickListener {
            val clip = ClipData.newPlainText("message", message)
            (activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(clip)
            Toast.makeText(activity, R.string.message_copied_to_clipboard, Toast.LENGTH_SHORT).show()

            dismiss()
        }

        val messageTv = contentView.findViewById<View>(R.id.message) as TextView
        messageTv.text = message

        return contentView
    }
}
