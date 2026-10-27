package com.codedroid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

class EditorFragment : Fragment() {

    companion object {
        fun newInstance(path: String) = EditorFragment().apply {
            arguments = Bundle().apply { putString("path", path) }
        }
    }

    private lateinit var editor: CodeEditor

    fun getCode(): String = editor.text.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        editor = CodeEditor(requireContext())
        editor.setEditorLanguage(JavaLanguage())
        editor.typefaceText = android.graphics.Typeface.MONOSPACE
        editor.setTextSize(14f)

        val path = arguments?.getString("path")
        if (path != null) editor.setText(File(path).readText())

        return editor
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editor.release()
    }
}
