package com.netspeedpro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import java.io.IOException

object SpeedTestManager {

    private val client = OkHttpClient.Builder()
        .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private const val BASE = "https://speed.cloudflare.com"

    suspend fun runPingTest(): Int = withContext(Dispatchers.IO) {
        val times = mutableListOf<Long>()
        repeat(5) { i ->
            try {
                val req = Request.Builder().url("$BASE/__ping?t=${System.currentTimeMillis()}").build()
                val start = System.currentTimeMillis()
                client.newCall(req).execute().use { it.body?.bytes() }
                times += System.currentTimeMillis() - start
            } catch (_: Exception) {}
            if (i < 4) Thread.sleep(80)
        }
        if (times.isEmpty()) return@withContext 999
        val sorted = times.sorted()
        val trimmed = if (sorted.size > 2) sorted.drop(1).dropLast(1) else sorted
        trimmed.average().toInt()
    }

    suspend fun runDownloadTest(onProgress: (speedMbps: Float) -> Unit): Float =
        withContext(Dispatchers.IO) {
            val req = Request.Builder()
                .url("$BASE/__down?bytes=10000000")
                .build()

            val startTime = System.currentTimeMillis()
            var lastBytes = 0L
            var lastTime = startTime
            val speeds = mutableListOf<Float>()
            var totalBytes = 0L

            try {
                client.newCall(req).execute().use { resp ->
                    val source = resp.body?.source() ?: return@withContext 0f
                    val buf = Buffer()
                    while (!source.exhausted()) {
                        val read = source.read(buf, 8192)
                        if (read == -1L) break
                        totalBytes += read
                        buf.skip(buf.size)

                        val now = System.currentTimeMillis()
                        val elapsed = (now - lastTime) / 1000.0
                        if (elapsed >= 0.25 && totalBytes > lastBytes) {
                            val bytes = totalBytes - lastBytes
                            val mbps = (bytes * 8.0 / elapsed / 1_000_000).toFloat()
                            if (mbps in 0.01f..10_000f) {
                                speeds += mbps
                                val recent = speeds.takeLast(4)
                                onProgress(recent.average().toFloat())
                            }
                            lastBytes = totalBytes
                            lastTime = now
                        }
                    }
                }
                val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
                if (totalTime <= 0 || totalBytes == 0L) return@withContext 0f
                (totalBytes * 8.0 / totalTime / 1_000_000).toFloat()
            } catch (_: IOException) { 0f }
        }

    suspend fun runUploadTest(onProgress: (speedMbps: Float) -> Unit): Float =
        withContext(Dispatchers.IO) {
            val bytes = 3 * 1024 * 1024
            val data = ByteArray(bytes) { 0x61 }

            val body = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = bytes.toLong()
                override fun writeTo(sink: okio.BufferedSink) {
                    val startTime = System.currentTimeMillis()
                    var lastSent = 0L
                    var lastTime = startTime
                    val speeds = mutableListOf<Float>()
                    val chunkSize = 16_384
                    var offset = 0

                    while (offset < bytes) {
                        val end = minOf(offset + chunkSize, bytes)
                        sink.write(data, offset, end - offset)
                        sink.flush()
                        val sent = end.toLong()

                        val now = System.currentTimeMillis()
                        val elapsed = (now - lastTime) / 1000.0
                        if (elapsed >= 0.25 && sent > lastSent) {
                            val chunk = sent - lastSent
                            val mbps = (chunk * 8.0 / elapsed / 1_000_000).toFloat()
                            if (mbps in 0.01f..10_000f) {
                                speeds += mbps
                                onProgress(speeds.takeLast(4).average().toFloat())
                            }
                            lastSent = sent
                            lastTime = now
                        }
                        offset = end
                    }
                }
            }

            val req = Request.Builder().url("$BASE/__up").post(body).build()
            val startTime = System.currentTimeMillis()
            return@withContext try {
                client.newCall(req).execute().use { }
                val totalTime = (System.currentTimeMillis() - startTime) / 1000.0
                (bytes * 8.0 / totalTime / 1_000_000).toFloat()
            } catch (_: IOException) { 0f }
        }
}
