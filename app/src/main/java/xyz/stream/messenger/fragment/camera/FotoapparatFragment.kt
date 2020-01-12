package xyz.stream.messenger.fragment.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.view.CameraView
import xyz.stream.messenger.R
import xyz.stream.messenger.fragment.message.attach.AttachmentListener
import xyz.stream.messenger.fragment.message.send.PermissionHelper
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.util.ImageUtils
import xyz.stream.messenger.shared.util.TimeUtils
import xyz.stream.messenger.shared.util.listener.ImageSelectedListener
import java.io.File

class FotoapparatFragment : Fragment(), View.OnClickListener {

    companion object {
        fun getInstance() = FotoapparatFragment()
    }

    lateinit var callback: ImageSelectedListener
    private var isPortrait = false

    private lateinit var loading: View
    private lateinit var cameraView: CameraView
    private lateinit var fotoapparat: Fotoapparat

    private var takePicture: View? = null
    private var flipPicture: View? = null
    private var fullscreen: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_fotoapparat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading = view.findViewById(R.id.loading)
        cameraView = view.findViewById(R.id.camera_view)
        fotoapparat = Fotoapparat(
                context = activity!!,
                view = cameraView
        )

        takePicture = view.findViewById(R.id.picture)
        flipPicture = view.findViewById(R.id.flip_picture)
        fullscreen = view.findViewById(R.id.fullscreen)

        takePicture?.setOnClickListener(this)
        flipPicture?.setOnClickListener(this)
        fullscreen?.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        startCamera()
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.picture -> {
                loading.visibility = View.VISIBLE

                takePicture?.isEnabled = false
                flipPicture?.isEnabled = false
                fullscreen?.isEnabled = false

                val activity = activity!!
                val file = File(activity.filesDir, "${TimeUtils.now}")
                fotoapparat.takePicture()
                        .saveToFile(file)
                        .whenAvailable {
                            val uri = ImageUtils.createContentUri(activity, file)
                            activity.runOnUiThread {
                                callback.onImageSelected(uri, MimeType.IMAGE_JPEG, true)
                            }
                        }
            }
            R.id.flip_picture -> {
                fotoapparat.switchTo(
                        lensPosition = if (isPortrait) back() else front(),
                        cameraConfiguration = CameraConfiguration.default()
                )

                isPortrait = !isPortrait
            }
            R.id.fullscreen -> {
                val startCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startCamera.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getUriForPhotoCaptureIntent(activity!!))

                try {
                    activity!!.startActivityForResult(startCamera, AttachmentListener.RESULT_CAPTURE_IMAGE_REQUEST)
                } catch (e: Exception) {
                    Toast.makeText(activity, "Please grant permission to start the camera.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startCamera() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PermissionHelper.PERMISSION_CAMERA_REQUEST)
            return
        }

        fotoapparat.start()
    }

    fun stopCamera() {
        fotoapparat.stop()
    }

}