package com.frame.fralib

import android.Manifest
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.frame.fralib.fralib.FraLib
import com.frame.fralib.fralib.FraLibCallBack
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "WebViewConstVal"
class WebViewFragment : Fragment() {
    private val dataStore by activityViewModels<BigFishGoldDataStore>()
    private lateinit var bigFishGoldPhoto: Uri
    private lateinit var bigFishGoldFilePathFromChrome: ValueCallback<Array<Uri>>
    private lateinit var abigFishGoldRequestFromChrome: PermissionRequest
    private lateinit var webview: FraLib

    val aviaPlinkTakeFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        helpGetFile(uri = it, filePathFromChrom = bigFishGoldFilePathFromChrome)
    }

    val aviaPlinkTakePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it == true) {
            helpTakePhoto(photo = bigFishGoldPhoto, filePathFromChrom = bigFishGoldFilePathFromChrome)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webview.canGoBack()) {
                        webview.goBack()
                    } else if (dataStore.bigFishGoldFFWSeVi.size > 1) {
                        this.isEnabled = false
                        dataStore.bigFishGoldFFWSeVi.removeLast()
                        webview.destroy()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dataStore.bigFishGoldFFWSeVi.isEmpty()) {
            Log.d(TAG, "First open")
            webview = FraLib(requireContext(), object : FraLibCallBack {
                override fun handleCreateWebWindowRequest(fraLib: FraLib) {
                    Log.d(TAG, "HandleCreateWebWindowRequest")
                    dataStore.bigFishGoldFFWSeVi.add(fraLib)
                    findNavController().navigate(R.id.action_webViewFragment_self)
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    Log.d(TAG, "onPermissionRequest")
                    if (request != null) {
                        abigFishGoldRequestFromChrome = request
                    }
                    Dexter.withContext(requireActivity())
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                abigFishGoldRequestFromChrome.grant(abigFishGoldRequestFromChrome.resources)
                            }

                            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: com.karumi.dexter.listener.PermissionRequest?,
                                p1: PermissionToken?
                            ) {

                            }

                        })
                        .check()
                }

                override fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>?) {
                    Log.d(TAG, "onShowFileChooser")
                    if (filePathCallback != null) {
                        bigFishGoldFilePathFromChrome = filePathCallback
                    }
                    val listItems: Array<out String> =
                        arrayOf("Select from file", "To make a photo")
                    val listener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            0 -> {
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                    aviaPlinkTakeImagesPermissionAndroid13(aviaPlinkTakeFile)
                                } else {
                                    takeImagesPermission(aviaPlinkTakeFile)
                                }
                            }
                            1 -> {
                                Dexter.withContext(requireActivity())
                                    .withPermission(Manifest.permission.CAMERA)
                                    .withListener(object : PermissionListener {
                                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                            bigFishGoldPhoto = aviaPlinkSavePhoto()
                                            aviaPlinkTakePhoto.launch(bigFishGoldPhoto)
                                        }

                                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            p0: com.karumi.dexter.listener.PermissionRequest?,
                                            p1: PermissionToken?
                                        ) {

                                        }

                                    })
                                    .check()
                            }
                        }
                    }
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Choose a method")
                        .setItems(listItems, listener)
                        .setCancelable(true)
                        .setOnCancelListener {
                            filePathCallback?.onReceiveValue(arrayOf(Uri.EMPTY))
                        }
                        .create()
                        .show()
                }

            })
            webview.fLoad("https://lk.nsq.market/ru/tools/testing")
            dataStore.bigFishGoldFFWSeVi.add(webview)
        } else {
            Log.d(TAG, "Second open")
            Log.d(TAG, "WebView list : ${dataStore.bigFishGoldFFWSeVi.size}")
            webview = dataStore.bigFishGoldFFWSeVi.last()
        }
        return webview
    }

    private fun aviaPlinkSavePhoto() : Uri {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val df = sdf.format(Date())
        val dir = requireContext().filesDir.absoluteFile
        if (!dir.exists()) {
            dir.mkdir()
        }
        return FileProvider.getUriForFile(
            requireContext(),
            AVIA_PLINK_AUTHORITY[0][0][0][0][0][0],
            File(dir, "/$df.jpg")
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun aviaPlinkTakeImagesPermissionAndroid13(takeFile: ActivityResultLauncher<String>) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_MEDIA_IMAGES)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    takeFile.launch("*/*")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            })
            .check()
    }

    private fun helpGetFile(uri: Uri?, filePathFromChrom: ValueCallback<Array<Uri>>) {
        filePathFromChrom.onReceiveValue(arrayOf(uri ?: Uri.EMPTY))
    }

    private fun helpTakePhoto(photo: Uri, filePathFromChrom: ValueCallback<Array<Uri>>) {
        filePathFromChrom.onReceiveValue(arrayOf(photo))
    }

    private fun helpTakePhotoFromChromPermission(requestFromChrome: PermissionRequest) {
        requestFromChrome.grant(requestFromChrome.resources)
    }


    private fun takeImagesPermission(takeFile: ActivityResultLauncher<String>) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    takeFile.launch("*/*")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            })
            .check()
    }

    companion object {
        private val AVIA_PLINK_AUTHORITY = arrayOf(arrayOf(arrayOf(arrayOf(arrayOf(arrayOf("com.aviaplink.fortune"))))))
    }

}