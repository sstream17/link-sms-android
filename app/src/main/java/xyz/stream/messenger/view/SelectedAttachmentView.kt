package xyz.stream.messenger.view

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import xyz.stream.messenger.fragment.message.attach.AttachmentManager
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import java.io.File

class SelectedAttachmentView(private val context: Context) {

    lateinit var mediaUri: Uri
    lateinit var mimeType: String
    var view: View = LayoutInflater.from(context).inflate(R.layout.item_attached_image, null, false)

    private val editImage: View? by lazy { view.findViewById<View>(R.id.edit_image) }
    private val editImageBackground: CircleImageView? by lazy { view.findViewById<CircleImageView>(R.id.edit_image_background) }
    private val removeImage: View by lazy { view.findViewById<View>(R.id.remove_image) }
    private val removeImageBackground: CircleImageView by lazy { view.findViewById<CircleImageView>(R.id.remove_image_background) }
    private val attachedImage: ImageView by lazy { view.findViewById<View>(R.id.attached_image) as ImageView }

    fun setup(attachmentManager: AttachmentManager, mediaUri: Uri, mimeType: String) {
        this.mediaUri = mediaUri
        this.mimeType = mimeType

        var color = attachmentManager.argManager.color
        var colorDark = attachmentManager.argManager.colorDark
        var colorAccent = attachmentManager.argManager.colorAccent

        if (Settings.useGlobalThemeColor) {
            color = Settings.mainColorSet.color
            colorDark = Settings.mainColorSet.colorDark
            colorAccent = Settings.mainColorSet.colorAccent
        }

        if (MimeType.isStaticImage(mimeType)) {
            editImageBackground?.setImageDrawable(ColorDrawable(colorAccent))
            editImage?.setOnClickListener {
                try {
                    val options = UCrop.Options()
                    options.setToolbarColor(color)
                    options.setStatusBarColor(colorDark)
                    options.setActiveWidgetColor(colorAccent)
                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    options.setCompressionQuality(100)
                    options.setFreeStyleCropEnabled(true)

                    val destination = File.createTempFile("ucrop", "jpg", context.cacheDir)
                    UCrop.of(mediaUri, Uri.fromFile(destination))
                            .withOptions(options)
                            .start(context as Activity)
                    attachmentManager.editingImage(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            editImage?.visibility = View.GONE
        }

        removeImageBackground.setImageDrawable(ColorDrawable(colorAccent))
        removeImage.setOnClickListener {
            attachmentManager.removeAttachment(mediaUri)
        }

        attachedImage.clipToOutline = true

        when {
            MimeType.isAudio(mimeType) -> {
                attachedImage.setImageResource(R.drawable.ic_audio_sent)
                attachedImage.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
            MimeType.isVcard(mimeType) -> {
                attachedImage.setImageResource(R.drawable.ic_contacts)
                attachedImage.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
            else -> {
                Glide.with(context).load(mediaUri)
                        .apply(RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .placeholder(xyz.stream.messenger.R.drawable.ic_image_sending))
                        .into(attachedImage)
            }
        }
    }

}