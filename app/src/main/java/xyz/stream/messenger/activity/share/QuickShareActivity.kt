package xyz.stream.messenger.activity.share

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import com.android.ex.chips.BaseRecipientAdapter
import com.android.ex.chips.RecipientEditTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xyz.klinker.android.floating_tutorial.FloatingTutorialActivity
import xyz.klinker.android.floating_tutorial.TutorialPage
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.compose.ComposeActivity
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.KeyboardLayout
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.ContactUtils
import xyz.stream.messenger.shared.util.KeyboardLayoutHelper
import android.util.TypedValue
import android.widget.*
import xyz.klinker.android.floating_tutorial.util.DensityConverter
import android.view.inputmethod.InputMethodManager
import xyz.stream.messenger.activity.compose.ShareData


class QuickShareActivity : FloatingTutorialActivity() {
    private val page: QuickSharePage by lazy { QuickSharePage(this) }
    override fun getPages() = listOf(page)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShareIntentHandler(page).handle(intent)
    }
}

class QuickSharePage(val activity: QuickShareActivity) : TutorialPage(activity) {

    val contactEntry: RecipientEditTextView by lazy { findViewById<RecipientEditTextView>(R.id.contact_entry) }
    val messageEntry: EditText by lazy { findViewById<EditText>(R.id.message_entry) }
    private val imagePreview: ImageView by lazy { findViewById<ImageView>(R.id.attached_image) }

    var mediaData: String? = null
    var mimeType: String? = null

    var sendClicked = false

    override fun initPage() {
        setContentView(R.layout.page_quick_share)
        setNextButtonText(R.string.send)
        setBackgroundColorResource(R.color.background)

        val frame = findViewById<FrameLayout>(R.id.tutorial_page_content)
        val linear = frame.parent as LinearLayout
        val card = linear.parent as View

        val layoutParams = card.layoutParams as FrameLayout.LayoutParams
        layoutParams.bottomMargin = DensityConverter.toDp(context, 72)

        card.layoutParams = layoutParams
        card.invalidate()

        if (Settings.isCurrentlyDarkTheme(activity)) {
            Handler().post { setProgressIndicatorColorResource(R.color.tutorial_dark_background_indicator) }
        }

        findViewById<View>(R.id.top_background).setBackgroundColor(Settings.mainColorSet.color)
        ColorUtils.setCursorDrawableColor(messageEntry, Settings.mainColorSet.colorAccent)
        KeyboardLayoutHelper.applyLayout(messageEntry, KeyboardLayout.SEND)
        prepareContactEntry()

        val sendButton = findViewById<View>(R.id.tutorial_next_button)
        sendButton.setOnClickListener {
            if (sendClicked) {
                return@setOnClickListener
            } else if (contactEntry.recipients.isNotEmpty() && ShareSender(this).sendMessage()) {
                sendClicked = true

                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(messageEntry.windowToken, 0)

                activity.finishAnimated()
            }
        }

        messageEntry.setOnEditorActionListener { _, actionId, keyEvent ->
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN &&
                    keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_SEND) {
                sendButton.performClick()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)

        val progressIndicator = findViewById<LinearLayout>(R.id.tutorial_progress)
        progressIndicator.setOnClickListener {
            activity.startActivity(Intent(activity, ComposeActivity::class.java))
            activity.finish()
        }

        contactEntry.post { contactEntry.requestFocus() }
    }

    fun setContacts(phoneNumbers: List<String>) {
        for (number in phoneNumbers) {
            val name = ContactUtils.findContactNames(number, activity)
            val image = ContactUtils.findImageUri(number, activity)

            if (image != null) {
                contactEntry.post { contactEntry.submitItem(name, number, Uri.parse("$image/photo")) }
            } else {
                contactEntry.post { contactEntry.submitItem(name, number) }
            }
        }

        contactEntry.post { messageEntry.requestFocus() }
    }

    fun setData(data: ShareData) {
        setData(listOf(data))
    }

    fun setData(data: List<ShareData>) {
        data.forEach {
            when {
                it.mimeType == MimeType.TEXT_PLAIN -> messageEntry.setText(it.data)
                it.mimeType == MimeType.IMAGE_GIF -> Glide.with(activity).asGif().apply(RequestOptions().centerCrop()).load(it.data).into(imagePreview)
                MimeType.isStaticImage(it.mimeType) -> Glide.with(activity).asBitmap().apply(RequestOptions().centerCrop()).load(it.data).into(imagePreview)
                MimeType.isVideo(it.mimeType) -> {
                    imagePreview.setImageResource(R.drawable.ic_play_sent)
                    imagePreview.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText))
                }
                MimeType.isAudio(it.mimeType) -> {
                    imagePreview.setImageResource(R.drawable.ic_audio_sent)
                    imagePreview.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText))
                }
            }

            if (it.mimeType != MimeType.TEXT_PLAIN) {
                imagePreview.visibility = View.VISIBLE

                this.mediaData = it.data
                this.mimeType = it.mimeType
            }
        }

        messageEntry.post { if (contactEntry.text.isNotEmpty()) {
            messageEntry.requestFocus()
            messageEntry.setSelection(messageEntry.text.length)
        } }
    }

    private fun prepareContactEntry() {
        val adapter = BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, context)
        adapter.isShowMobileOnly = Settings.mobileOnly

        contactEntry.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        contactEntry.highlightColor = Settings.mainColorSet.colorAccent
        contactEntry.setAdapter(adapter)
    }
}