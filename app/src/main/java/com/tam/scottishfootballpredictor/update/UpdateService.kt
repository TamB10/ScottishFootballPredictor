package com.tam.scottishfootballpredictor.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File

interface UpdateApi {
    @GET("version.json")
    suspend fun getLatestVersion(): VersionInfo
}

data class VersionInfo(
    val version: String,
    val downloadUrl: String,
    val changelog: String
)

class UpdateService(private val context: Context) {
    private val retrofit = Retrofit.Builder()
        // Note the / at the end of the URL
        .baseUrl("https://raw.githubusercontent.com/TamB10/ScottishFootballPredictor/main/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(UpdateApi::class.java)
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun checkForUpdates(currentVersion: String): VersionInfo? = withContext(Dispatchers.IO) {
        try {
            val latestVersion = api.getLatestVersion()
            if (latestVersion.version != currentVersion) {
                return@withContext latestVersion
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun downloadUpdate(versionInfo: VersionInfo): Long {
        val uri = Uri.parse(versionInfo.downloadUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle("Downloading update")
            setDescription("Downloading version ${versionInfo.version}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "scottish-predictor-${versionInfo.version}.apk"
            )
        }
        return downloadManager.enqueue(request)
    }

    fun installUpdate(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val localUri = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            )
            val file = File(Uri.parse(localUri).path!!)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val install = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(install)
        }
        cursor.close()
    }
}