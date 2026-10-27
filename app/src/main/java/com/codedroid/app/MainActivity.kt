package com.codedroid.app

import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.codedroid.app.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val openFiles = mutableListOf<File>()
    private val rootDir: File get() =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = openFiles.size
            override fun createFragment(pos: Int) =
                EditorFragment.newInstance(openFiles[pos].absolutePath)
        }

        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos ->
            tab.text = openFiles[pos].name
        }.attach()

        refreshFileList()

        binding.bottomNav.setOnItemSelectedListener { item ->
            val pos = binding.pager.currentItem
            when (item.itemId) {
                R.id.action_save -> {
                    saveCurrent(pos); true
                }
                R.id.action_run -> {
                    runCurrent(pos); true
                }
                R.id.action_plugins -> {
                    startActivity(android.content.Intent(this, PluginsActivity::class.java)); true
                }
                else -> false
            }
        }
    }

    private fun refreshFileList() {
        val files = rootDir.listFiles()?.sortedBy { !it.isDirectory }
            ?.map { (if (it.isDirectory) "📁 " else "📄 ") + it.name } ?: emptyList()
        binding.fileList.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, files
        )
        binding.fileList.setOnItemClickListener { _, _, i, _ ->
            val f = rootDir.listFiles()?.sortedBy { !it.isDirectory }?.get(i) ?: return@setOnItemClickListener
            if (f.isDirectory) { refreshFileList(); return@setOnItemClickListener }
            openFile(f)
            binding.drawer.closeDrawer(androidx.core.view.GravityCompat.START)
        }
    }

    private fun openFile(f: File) {
        if (openFiles.none { it.absolutePath == f.absolutePath }) {
            openFiles.add(f)
            binding.pager.adapter?.notifyDataSetChanged()
            binding.tabs.removeAllTabs()
            openFiles.forEachIndexed { i, file ->
                binding.tabs.addTab(binding.tabs.newTab().setText(file.name).setTag(i))
            }
        }
        binding.pager.setCurrentItem(openFiles.indexOfFirst { it.absolutePath == f.absolutePath }, true)
    }

    private fun saveCurrent(pos: Int) {
        val frag = supportFragmentManager.findFragmentByTag("f$pos") as? EditorFragment
            ?: return Toast.makeText(this, "Нет открытого файла", Toast.LENGTH_SHORT).show()
        openFiles[pos].writeText(frag.getCode())
        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
    }

    private fun runCurrent(pos: Int) {
        val f = openFiles[pos]
        when (f.extension) {
            "js" -> runJs(f.readText())
            "py" -> Toast.makeText(this,
                "Python требует Chaquopy (см. README)", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Запуск .${f.extension} пока не поддерживается",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun runJs(code: String) {
        Thread {
            try {
                val runtime = app.cash.quickjs.QuickJs.create()
                val result = runtime.evaluate(code) ?: "null"
                runOnUiThread { showOutput(result.toString()) }
                runtime.close()
            } catch (e: Exception) {
                runOnUiThread { showOutput("Ошибка: ${e.message}") }
            }
        }.start()
    }

    private fun showOutput(text: String) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Вывод")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }
}
