package xyz.stream.messenger.fragment.message.attach

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialcamera.MaterialCamera
import xyz.stream.messenger.R
import xyz.stream.messenger.fragment.ScheduledMessagesFragment
import xyz.stream.messenger.fragment.camera.Camera2BasicFragment
import xyz.stream.messenger.fragment.camera.FotoapparatFragment
import xyz.stream.messenger.fragment.message.MessageListFragment
import xyz.stream.messenger.fragment.message.send.PermissionHelper
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.FeatureFlags
import xyz.stream.messenger.shared.data.MmsSettings
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.util.DensityUtil
import xyz.stream.messenger.shared.util.PhoneNumberUtils
import xyz.stream.messenger.shared.util.SmsMmsUtils
import xyz.stream.messenger.shared.util.TvUtils
import xyz.stream.messenger.shared.util.listener.TextSelectedListener
import xyz.stream.messenger.view.*
import java.lang.Exception

@Suppress("DEPRECATION")
class AttachmentInitializer(private val fragment: MessageListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val argManager 
        get() = fragment.argManager
    private val attachListener
        get() = fragment.attachListener

    private val attachLayoutStub: ViewStub by lazy { fragment.rootView!!.findViewById<View>(R.id.attach_stub) as ViewStub }
    private val send: View by lazy { fragment.rootView!!.findViewById<View>(R.id.send) }
    private val attach: View by lazy { fragment.rootView!!.findViewById<View>(R.id.attach) }
    private val scheduledMessages: View by lazy { fragment.rootView!!.findViewById<View>(R.id.view_scheduled_messages) }
    private val selectSim: View by lazy { fragment.rootView!!.findViewById<View>(R.id.select_sim) }
    private val attachLayout: View by lazy { fragment.rootView!!.findViewById<View>(R.id.attach_layout) }
    private val attachHolder: FrameLayout by lazy { fragment.rootView!!.findViewById<View>(R.id.attach_holder) as FrameLayout }
    private val attachButtonHolder: LinearLayout by lazy { fragment.rootView!!.findViewById<View>(R.id.attach_button_holder) as LinearLayout }
    private val dragDismissFrameLayout: View by lazy { fragment.rootView!! }

    var fotoapparatFragment: FotoapparatFragment? = null

    fun initAttachHolder() {
        if (!TvUtils.hasTouchscreen(activity)) {
            attach.visibility = View.GONE
            send.nextFocusDownId = R.id.message_entry
        }

        attach.setOnClickListener {
            if (attachLayoutStub.parent != null) {
                initAttachStub()
            }

            val animator: ValueAnimator
            if (attachLayout.visibility == View.VISIBLE) {
                if (getBoldedAttachHolderPosition() == -1 && attach.isSoundEffectsEnabled) {
                    attachImage(true)
                    return@setOnClickListener
                }

                dragDismissFrameLayout.isEnabled = true
                animator = ValueAnimator.ofInt(attachLayout.height, 0)
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        attachHolder.removeAllViews()
                        attachLayout.visibility = View.GONE
                    }
                })
            } else {
                dragDismissFrameLayout.isEnabled = false
                attachImage(true)
                attachLayout.visibility = View.VISIBLE
                animator = ValueAnimator.ofInt(0,
                        fragment.resources.getDimensionPixelSize(R.dimen.attach_menu_height))
            }

            val params = attachLayout.layoutParams as ViewGroup.MarginLayoutParams

            animator.addUpdateListener { valueAnimator ->
                params.height = valueAnimator.animatedValue as Int
                attachLayout.requestLayout()
            }

            animator.duration = 200
            animator.start()
        }

        scheduledMessages.setOnClickListener {
            val visible = attachLayoutStub.parent == null && attachLayout.visibility == View.VISIBLE
            val showingScheduled = try { getBoldedAttachHolderPosition() == -1 } catch (e: Exception) { false }

            if (visible && showingScheduled) {
                activity?.onBackPressed() // dismiss
            } else if (visible && !showingScheduled) {
                viewScheduledMessages() // show scheduled
            } else {
                // animate visible and show scheduled
                attach.performClick()
                viewScheduledMessages()
            }
        }

        Handler().postDelayed({
            if (selectSim.visibility == View.VISIBLE) {
                return@postDelayed
            }

            Thread { try {
                val thisMatcher = SmsMmsUtils.createIdMatcher(PhoneNumberUtils.clearFormattingAndStripStandardReplacements(fragment.argManager.phoneNumbers)).default
                val scheduledMessages = DataSource.getScheduledMessagesAsList(activity!!).filter {
                    SmsMmsUtils.createIdMatcher(PhoneNumberUtils.clearFormattingAndStripStandardReplacements(it.to!!)).default == thisMatcher
                }

                if (scheduledMessages.isNotEmpty()) {
                    activity?.runOnUiThread {
                        val animator = ValueAnimator.ofInt(0, DensityUtil.toDp(activity!!, 32))
                        val params = this.scheduledMessages.layoutParams as ViewGroup.MarginLayoutParams

                        params.width = 0
                        this.scheduledMessages.requestLayout()
                        this.scheduledMessages.visibility = View.VISIBLE

                        animator.addUpdateListener { valueAnimator ->
                            params.width = valueAnimator.animatedValue as Int
                            this.scheduledMessages.requestLayout()
                        }

                        animator.duration = 200
                        animator.start()
                    }
                }
            } catch (e: Throwable) {} }.start()
        }, 500)
    }

    private fun initAttachStub() {
        attachLayoutStub.inflate()

        val attachImage = fragment.rootView!!.findViewById<View>(R.id.attach_image) as ImageButton
        val captureImage = fragment.rootView!!.findViewById<View>(R.id.capture_image) as ImageButton
        val attachGif = fragment.rootView!!.findViewById<View>(R.id.attach_gif) as ImageButton
        val attachSticker = fragment.rootView!!.findViewById<View>(R.id.attach_sticker) as ImageButton
        val recordVideo = fragment.rootView!!.findViewById<View>(R.id.record_video) as ImageButton
        val recordAudio = fragment.rootView!!.findViewById<View>(R.id.record_audio) as ImageButton
        val attachLocation = fragment.rootView!!.findViewById<View>(R.id.attach_location) as ImageButton
        val attachContact = fragment.rootView!!.findViewById<View>(R.id.attach_contact) as ImageButton
        val applyTemplate = fragment.rootView!!.findViewById<View>(R.id.apply_template) as ImageButton

        attachImage.setOnClickListener { attachImage() }
        captureImage.setOnClickListener { captureImage() }
        attachGif.setOnClickListener { attachGif() }
        attachSticker.setOnClickListener { attachSticker() }
        recordVideo.setOnClickListener { recordVideo() }
        recordAudio.setOnClickListener { recordAudio() }
        attachLocation.setOnClickListener { attachLocation() }
        attachContact.setOnClickListener { attachContact() }
        applyTemplate.setOnClickListener { applyTemplate() }

        var colorButtonsDark = false
//        if (Settings.useGlobalThemeColor) {
//            attachButtonHolder.setBackgroundColor(Settings.mainColorSet.color)
//            if (!ColorUtils.isColorDark(Settings.mainColorSet.color)) {
//                colorButtonsDark = true
//            }
//        } else {
//            attachButtonHolder.setBackgroundColor(fragment.argManager.color)
//            if (!ColorUtils.isColorDark(fragment.argManager.color)) {
//                colorButtonsDark = true
//            }
//        }
        if (Settings.baseTheme == BaseTheme.BLACK) {
            attachButtonHolder.setBackgroundColor(Color.BLACK)
        } else {
            attachButtonHolder.setBackgroundColor(fragment.resources.getColor(R.color.drawerBackground))
        }

        if (!Settings.isCurrentlyDarkTheme(fragment.rootView!!.context)) {
            colorButtonsDark = true
        }

        if (colorButtonsDark) {
            val list = ColorStateList.valueOf(fragment.resources.getColor(R.color.lightToolbarTextColor))
            attachImage.imageTintList = list
            captureImage.imageTintList = list
            attachGif.imageTintList = list
            attachSticker.imageTintList = list
            recordVideo.imageTintList = list
            recordAudio.imageTintList = list
            attachLocation.imageTintList = list
            attachContact.imageTintList = list
            applyTemplate.imageTintList = list
        }
    }

    private fun viewScheduledMessages() {
        prepareAttachHolder(-1)

        val thisMatcher = SmsMmsUtils.createIdMatcher(PhoneNumberUtils.clearFormattingAndStripStandardReplacements(fragment.argManager.phoneNumbers)).default
        val fragment = ScheduledMessagesFragment.newInstance(thisMatcher)
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.attach_holder, fragment)?.commit()
        attachHolder.backgroundTintList = ColorStateList.valueOf(this.fragment.resources.getColor(R.color.background))
    }

    internal fun attachImage(alwaysOpen: Boolean = false) {
        if (!alwaysOpen && getBoldedAttachHolderPosition() == 0 || activity == null) {
            return
        }

        try {
            prepareAttachHolder(0)
            if (ContextCompat.checkSelfPermission(activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                attachHolder.addView(AttachImageView(activity!!, attachListener,
                        if (Settings.useGlobalThemeColor)  Settings.mainColorSet.color else argManager.color))
            } else {
                attachPermissionRequest(PermissionHelper.PERMISSION_STORAGE_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } catch (e: NullPointerException) {

        }
    }

    fun captureImage(alwaysOpen: Boolean = false) {
        if (!alwaysOpen && getBoldedAttachHolderPosition() == 1) {
            return
        }

        prepareAttachHolder(1)

        fotoapparatFragment = FotoapparatFragment.getInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.attach_holder, fotoapparatFragment!!)?.commit()
        fotoapparatFragment?.callback = attachListener
    }

    private fun attachGif() {
        if (getBoldedAttachHolderPosition() == 2) {
            return
        }

        prepareAttachHolder(2)
        attachHolder.addView(GiphySearchView(activity!!, attachListener, false))
    }

    private fun attachSticker() {
        if (getBoldedAttachHolderPosition() == 3) {
            return
        }

        prepareAttachHolder(3)
        attachHolder.addView(GiphySearchView(activity!!, attachListener, true))
    }

    private fun recordVideo() {
        if (getBoldedAttachHolderPosition() == 4 || activity == null) {
            return
        }

        prepareAttachHolder(4)

        val camera = MaterialCamera(activity!!)
                .saveDir(activity!!.filesDir.path)
                .qualityProfile(MaterialCamera.QUALITY_LOW)
                .maxAllowedFileSize(MmsSettings.maxImageSize)
                .allowRetry(false)
                .autoSubmit(true)
                .showPortraitWarning(false)

        if (Settings.useGlobalThemeColor) {
            camera.primaryColor(Settings.mainColorSet.color)
        } else {
            camera.primaryColor(argManager.color)
        }

        camera.start(AttachmentListener.RESULT_VIDEO_REQUEST)
    }

    internal fun recordAudio(alwaysOpen: Boolean = false) {
        if (!alwaysOpen && getBoldedAttachHolderPosition() == 5 || activity == null) {
            return
        }

        prepareAttachHolder(5)
        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            attachHolder.addView(RecordAudioView(activity!!, attachListener,
                    if (Settings.useGlobalThemeColor) Settings.mainColorSet.colorAccent else argManager.colorAccent))
        } else {
            attachPermissionRequest(PermissionHelper.PERMISSION_AUDIO_REQUEST,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
        }
    }

    internal fun attachLocation(alwaysOpen: Boolean = false) {
        if (!alwaysOpen && getBoldedAttachHolderPosition() == 6 || activity == null) {
            return
        }

        prepareAttachHolder(6)
        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            attachHolder.addView(AttachLocationView(activity!!, fragment.attachListener, attachListener,
                    if (Settings.useGlobalThemeColor) Settings.mainColorSet.colorAccent else argManager.colorAccent))
        } else {
            attachPermissionRequest(PermissionHelper.PERMISSION_LOCATION_REQUEST,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun attachContact() {
        if (getBoldedAttachHolderPosition() == 7 || activity == null) {
            return
        }

        prepareAttachHolder(7)
        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            attachHolder.addView(AttachContactView(activity!!, attachListener,
                    if (Settings.useGlobalThemeColor) Settings.mainColorSet.color else argManager.color))
        } else {
            attachPermissionRequest(PermissionHelper.PERMISSION_AUDIO_REQUEST,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun applyTemplate() {
        if (getBoldedAttachHolderPosition() == 8 || activity == null) {
            return
        }

        prepareAttachHolder(8)
        attachHolder.addView(TemplateManagerView(activity!!,
                if (Settings.useGlobalThemeColor) Settings.mainColorSet.colorAccent else argManager.colorAccent,
                object : TextSelectedListener {
                    override fun onTextSelected(text: String) {
                        fragment.onBackPressed()
                        attachListener.onTextSelected(text)
                    }
                }))
    }

    private fun attachPermissionRequest(permissionRequestCode: Int, vararg permissions: String) {
        LayoutInflater.from(activity).inflate(R.layout.permission_request, attachHolder, true)
        val request = attachHolder.findViewById<View>(R.id.permission_needed) as Button
        request.setOnClickListener { fragment.requestPermissions(permissions, permissionRequestCode) }
    }

    private fun prepareAttachHolder(positionToBold: Int) {
        fragment.dismissKeyboard()
        attachHolder.removeAllViews()
        fotoapparatFragment?.stopCamera()

        attachHolder.backgroundTintList = null

        for (i in 0 until attachButtonHolder.childCount) {
            if (positionToBold == i) {
                attachButtonHolder.getChildAt(i).alpha = 1.0f
            } else {
                attachButtonHolder.getChildAt(i).alpha = 0.5f
            }
        }
    }

    private fun getBoldedAttachHolderPosition(): Int {
        return (0 until attachButtonHolder.childCount).firstOrNull { attachButtonHolder.getChildAt(it).alpha == 1.0f }
                ?: -1
    }

    fun onClose() {
        fotoapparatFragment?.stopCamera()
    }
}