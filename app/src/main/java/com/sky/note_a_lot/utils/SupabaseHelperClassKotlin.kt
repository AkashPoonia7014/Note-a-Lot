package com.sky.note_a_lot.utils


import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Bucket
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val api_key = "sb_publishable_yDScMeSvhkUSpK98etC31A_mmiU5xp9"
private val service_roll_key_secret = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ3Z3hqeHZkd2Zobnlodm55ZXp3Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NzU0MDMyNCwiZXhwIjoyMDgzMTE2MzI0fQ.D9YRlA3ka9jT3gJdNshPKBUmD8U8NRJvgRjE8w16LEo"

object SupabaseHelperClassKotlin {

    private val supabase = createSupabaseClient(
        "https://rwgxjxvdwfhnyhvnyezw.supabase.co",
        api_key
    ) {
        install(Storage)
    }

    suspend fun uploadImage(bucketName: String, filePath: String, imageByteArray: ByteArray): String {

        val bucket = supabase.storage.from(bucketName)
        bucket.upload(filePath, imageByteArray, true)
        return bucket.publicUrl(filePath)
    }
    suspend fun deleteFolder(bucketName: String, folderPath: String) {

        val bucket = supabase.storage.from(bucketName)
        val files = bucket.list(folderPath)

        if (files.isEmpty()) return

        val pathsToDelete = mutableListOf<String>()
        files.forEach {
            if (it.name != null) {
                pathsToDelete.add("$folderPath/${it.name}")
            }
        }
        bucket.delete(pathsToDelete)

/*

        // this is does the same thing as the above iterative loop, it is more kotlin like
        val pathsTToDelete = files
            .mapNotNull { it.name }
            .map { "$folderPath/$it" }

*/
    }

    @JvmStatic
    fun uploadImageBlocking(bucketName: String, filePath: String, imageByteArray: ByteArray) : String = runBlocking {
        uploadImage(bucketName, filePath, imageByteArray)
    }


    @JvmStatic
    fun deleteFolderBlocking(bucketName: String, folderPath: String) = runBlocking {
        deleteFolder(bucketName, folderPath)
    }
}