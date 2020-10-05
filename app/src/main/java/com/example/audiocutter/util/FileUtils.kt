package com.example.audiocutter.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    private const val LOG_TAG = "FileUtils"
    private var contentUri: Uri? = null

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        // Check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                val fullPath = getPathFromExtSD(split)
                return if (fullPath !== "") {
                    fullPath
                } else {
                    null
                }
            } else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val id: String
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            val fileName = cursor.getString(0)
                            val path = Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                            if (!TextUtils.isEmpty(path)) {
                                return path
                            }
                        }
                    } finally {
                        cursor?.close()
                    }
                    id = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        val contentUriPrefixesToTry = arrayOf("content://downloads/public_downloads", "content://downloads/my_downloads")
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            return try {
                                val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))

                                // final Uri contentUri = ContentUris.withAppendedId(
                                //        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                                getDataColumn(context, contentUri, null, null)
                            } catch (e: NumberFormatException) {
                                // In Android 8 and Android P the id is not a number
                                uri.path!!.replaceFirst("^/document/raw:".toRegex(), "")
                                    .replaceFirst("^raw:".toRegex(), "")
                            }
                        }
                    }
                } else {
                    val id = DocumentsContract.getDocumentId(uri)
                    val isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null)
                    }
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            } /*else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context)
            }*/
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            /* if (isGoogleDriveUri(uri)) {
                 return getDriveFilePath(uri, context)
             }*/
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.N) {
                /* // return getFilePathFromURI(context,uri);
                 getMediaFilePathForN(uri, context)
                 // return getRealPathFromURI(context,uri);
             } else {*/
                return getDataColumn(context, uri, null, null)
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type = pathData[0]
        val relativePath = "/" + pathData[1]
        var fullPath = ""

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
        if (fileExists(fullPath)) {
            return fullPath
        }
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
        return if (fileExists(fullPath)) {
            fullPath
        } else fullPath
    }

    private fun getDriveFilePath(uri: Uri, context: Context): String {
        val contentResolver = context.contentResolver
        val returnCursor = contentResolver.query(uri, null, null, null, null)

        // Get the column indexes of the data in the Cursor,
        // move to the first row in the Cursor, get the data,
        // and display it.
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            // int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message!!)
        }
        return file.path
    }

    private fun getMediaFilePathForN(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)

        // Get the column indexes of the data in the Cursor,
        // move to the first row in the Cursor, get the data,
        // and display it.
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val file = File(context.filesDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message!!)
        }
        return file.path
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }
}