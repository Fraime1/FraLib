package com.frame.fralib.fralib


import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback

interface FraLibCallBack {
    fun handleCreateWebWindowRequest(fraLib: FraLib)

    fun onPermissionRequest(request: PermissionRequest?)

    fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>?)
}