package com.codedroid.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream

data class Extension(val id: String, val name: String, val vsixUrl: String)

class PluginsActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var listAdapter: ArrayAdapter<String>
    private var extensions = listOf<Extension>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.VERTICAL }
        val search = SearchView(this)
        val listView = ListView(this)
        layout.addView(search); layout.addView(listView)
        setContentView(layout)

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = listAdapter

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean {
                q?.let { searchMarketplace(it) }; return true
            }
            override fun onQueryTextChange(p0: String?) = false
        })

        listView.setOnItemClickListener { _, _, i, _ ->
            downloadVsix(extensions[i])
        }
    }

    private fun searchMarketplace(query: String) {
        Thread {
            try {
                val body = """
                {"filters":[{"criteria":[{"filterType":10,"value":"$query"}]}],
                 "flags":0x192}
                """.trimIndent().toRequestBody("application/json".toMediaType())
                val req = Request.Builder()
                    .url("https://marketplace.visualstudio.com/_apis/public/gallery/extensionquery")
                    .post(body).build()
                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body!!.string())
                val exts = json.getJSONArray("results").getJSONObject(0)
                    .getJSONArray("extensions")
                extensions = (0 until exts.length()).map { i ->
                    val e = exts.getJSONObject(i)
                    val pub = e.getJSONArray("publisher").getJSONObject(0).getString("publisherName")
                    val extName = e.getString("extensionName")
                    val vsix = e.getJSONArray("versions").getJSONObject(0)
                        .getJSONArray("files")
                        .let { f -> (0 until f.length()).map { f.getJSONObject(it) } }
                        .first { it.getString("assetType") == "Microsoft.VisualStudio.Services.VSIXPackage" }
                        .getString("source")
                    Extension("$pub.$extName", e.getString("displayName"), vsix)
                }
                runOnUiThread {
                    listAdapter.clear()
                    listAdapter.addAll(extensions.map { it.name })
                    listAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun downloadVsix(ext: Extension) {
        Thread {
            try {
                val req = Request.Builder().url(ext.vsixUrl).build()
                val bytes = client.newCall(req).execute().body!!.bytes()
                val pluginsDir = File(filesDir, "plugins")
                pluginsDir.mkdirs()
                // VSIX = ZIP: распаковываем, чтобы потом читать сниппеты/темы/грамматики
                ZipInputStream(bytes.inputStream()).use { zis ->
                    var entry = zis.nextEntry
                    val outDir = File(pluginsDir, ext.id)
                    while (entry != null) {
                        val outFile = File(outDir, entry.name)
                        if (entry.isDirectory) outFile.mkdirs()
                        else {
                            outFile.parentFile?.mkdirs()
                            outFile.writeBytes(zis.readBytes())
                        }
                        entry = zis.nextEntry
                    }
                }
                runOnUiThread {
                    Toast.makeText(this,
                        "${ext.name} установлен (${ext.id})", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
