package com.example.audiocutter.functions.mystudio.dialog

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.util.Utils
import kotlinx.android.synthetic.main.my_studio_dialog_rename.*

class RenameDialog : BaseDialog() {
    private lateinit var edtNewName: EditText
    private lateinit var tvCancel: TextView
    private lateinit var tvRename: TextView

    companion object {
        val TAG = "DeleteDialog"
        val BUNDLE_TYPE_KEY = "BUNDLE_TYPE_KEY"
        val BUNDLE_PATH_KEY = "BUNDLE_PATH_KEY"
        val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        lateinit var dialogListener: RenameDialogListener

        @JvmStatic
        fun newInstance(listener: RenameDialogListener, type: Int, filePath: String, name: String): RenameDialog {
            this.dialogListener = listener
            val dialog = RenameDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_TYPE_KEY, type)
            bundle.putString(BUNDLE_PATH_KEY, filePath)
            bundle.putString(BUNDLE_NAME_KEY, name)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
        /* dialog?.let {
             dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
             val width = ((requireContext().resources?.displayMetrics?.widthPixels)!! * 0.90).toInt()
             dialog!!.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
         }*/


    }

    override fun getLayoutResId(): Int {
        return R.layout.my_studio_dialog_rename
    }

    override fun initViews(view: View, savedInstanceState: Bundle?) {
        val newName = requireArguments().getString(BUNDLE_NAME_KEY, "")
        edtNewName = view.findViewById(R.id.edt_new_name)
        tvCancel = view.findViewById(R.id.tv_cancel_dialog)
        tvRename = view.findViewById(R.id.tv_rename_dialog)
        edtNewName.setText(newName)
        tvCancel.setOnClickListener {
            Utils.hideKeyboard(requireContext(), edtNewName)
            dismiss()
        }
        tvRename.setOnClickListener {
            if (checkValid(edt_new_name.text.toString())) {
                Utils.hideKeyboard(requireContext(), edtNewName)
                dialogListener.onRenameClick(edt_new_name.text.toString(), requireArguments().getInt(BUNDLE_TYPE_KEY), requireArguments().getString(BUNDLE_PATH_KEY, ""))
                dismiss()
            }
        }
        edtNewName.setSelection(newName.length)
        edtNewName.post {
            Utils.showKeyboard(requireContext(), edtNewName)
        }
    }
    private fun checkValid(name: String): Boolean {
        val typeFolder: Folder = when (requireArguments().getInt(BUNDLE_TYPE_KEY)) {
            0 -> Folder.TYPE_CUTTER

            1 -> Folder.TYPE_MERGER

            else -> Folder.TYPE_MIXER
        }
        if (name.isEmpty()) {
            edt_new_name.error = context?.resources?.getString(R.string.name_mustbenull)
            edt_new_name.requestFocus()
            return false
        }
        if (name.startsWith(".")) {
            edt_new_name.error = context?.resources?.getString(R.string.my_studio_screen_rename_error)
            edt_new_name.requestFocus()
            return false
        }
        if (ManagerFactory.getAudioFileManager()
                .checkFileNameDuplicate(edt_new_name.text.toString(), typeFolder)) {
            edt_new_name.error = context?.resources?.getString(R.string.filename_exist)
            edt_new_name.requestFocus()
            return false
        }
        return true
    }
}

interface RenameDialogListener {
    fun onRenameClick(name: String, type: Int, filePath: String)
}