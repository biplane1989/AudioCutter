package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseDialog
import com.example.audiocutter.core.audiomanager.Folder
import com.example.audiocutter.core.manager.ManagerFactory
import com.example.audiocutter.util.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.merge_audio_dialog.*

class MergeDialog : BaseDialog(), View.OnClickListener {

    private var countFile = 0
//    private lateinit var mCallback: MergeDialogListener


    companion object {
        val TAG = "MergeDialog"
        private const val BUNDLE_NAME_KEY = "BUNDLE_NAME_KEY"
        private const val SUGGESTION_NAME_KEY = "SUGGESTION_NAME_KEY"
        lateinit var mCallback: MergeDialogListener
        lateinit var mEdtName: EditText

        @JvmStatic
        fun newInstance(
            listener: MergeDialogListener,
            countFile: Int = 0,
            suggestionName: String
        ): MergeDialog {
            this.mCallback = listener
            val dialog = MergeDialog()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_NAME_KEY, countFile)
            bundle.putString(SUGGESTION_NAME_KEY, suggestionName)

            dialog.arguments = bundle
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.merge_audio_dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setStyle(STYLE_NO_TITLE, R.style.DialogGray)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEdtName = view.findViewById(R.id.edt_filename_dialog)
        countFile = requireArguments().getInt(BUNDLE_NAME_KEY)
        tv_count_file_dialog.text =
            "%d %s".format(countFile, requireContext().getString(R.string.file_merged))
        val newName = Utils.genAudioFileName(
            Folder.TYPE_MERGER, prefixName = arguments?.getString(
                SUGGESTION_NAME_KEY
            ) ?: ""
        )
        mEdtName.setText(newName)
        tv_cancel_dialog_merge.setOnClickListener(this)
        tv_ok_dialog_merge.setOnClickListener(this)
        mEdtName.setSelection(newName.length)
        mEdtName.post {
            Utils.showKeyboard(requireContext(), mEdtName)
        }


    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Utils.hideKeyboard(requireContext(), mEdtName)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_cancel_dialog_merge -> {
                mCallback.cancalKeybroad()
                dismiss()
            }
            R.id.tv_ok_dialog_merge -> {
                if (checkValid(edt_filename_dialog.text.toString())) {
                    mCallback.mergeAudioFile(edt_filename_dialog.text.toString())
                    dismiss()
                }
            }
        }
    }

    private fun checkValid(name: String): Boolean {
        if (name.isEmpty()) {
            edt_filename_dialog.error = getString(R.string.audio_name_is_not_empty)
            edt_filename_dialog.requestFocus()
            return false
        }
        if (ManagerFactory.getAudioFileManager().checkFileNameDuplicate(name, Folder.TYPE_MERGER)) {
            edt_filename_dialog.error = getString(R.string.audio_name_this_name_is_already_existed)
            edt_filename_dialog.requestFocus()
            return false
        }
        return true

    }

//    fun sendData(size: Int) {
//        countFile = size
//    }

    interface MergeDialogListener {
        fun mergeAudioFile(filename: String)
        fun cancalKeybroad()
    }
}