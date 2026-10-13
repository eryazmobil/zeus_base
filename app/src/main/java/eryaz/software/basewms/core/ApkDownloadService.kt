package eryaz.software.basewms.core

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import eryaz.software.basewms.BuildConfig
import eryaz.software.basewms.R
import eryaz.software.basewms.util.extensions.toast
import java.io.BufferedInputStream
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ApkDownloadService : Service() {
    private lateinit var apkZipUrl: String
    private lateinit var apkZipFileName: String
    private lateinit var apkFileName: String
    private var downloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                val zipFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    apkZipFileName
                )
                val outputDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                unzip(zipFile, outputDir)
                val apkFile = File(outputDir, apkFileName)
                installAPK(apkFile)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        registerReceiver(
            downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            RECEIVER_EXPORTED
        )

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        apkZipUrl = intent?.getStringExtra("apkZipUrl") ?: ""
        apkZipFileName = intent?.getStringExtra("apkZipFileName") ?: ""
        apkFileName = intent?.getStringExtra("apkFileName") ?: ""
        downloadAPKZip()
        return START_NOT_STICKY
    }

    private fun downloadAPKZip() {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(apkZipUrl)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle(getString(R.string.app_name) + " - " + BuildConfig.VERSION_NAME)
            setDescription(getString(R.string.plaese_wait))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkZipFileName)
        }

        downloadId = downloadManager.enqueue(request)

    }

    private fun unzip(zipFile: File, outputFile: File?) {
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInputStream ->
                var zipEntry: ZipEntry?
                while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                    val file = File(outputFile, zipEntry!!.name)
                    if (zipEntry!!.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fileOutputStream ->
                            val buffer = ByteArray(1024)
                            var count: Int
                            while (zipInputStream.read(buffer).also { count = it } != -1) {
                                fileOutputStream.write(buffer, 0, count)
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            toast(getString(R.string.error_occured))
        }
    }

    private fun installAPK(apkFile: File) {
        if (!packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }

        val apkUri = FileProvider.getUriForFile(this, "$packageName.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }


    companion object {
        fun startService(context: Context, url: String?, zipName: String?, apkFileName: String?) {
            val startIntent = Intent(context, ApkDownloadService::class.java)
            startIntent.putExtra("apkZipUrl", url)
            startIntent.putExtra("apkZipFileName", zipName)
            startIntent.putExtra("apkFileName", apkFileName)
            context.startService(startIntent)
        }
    }

}