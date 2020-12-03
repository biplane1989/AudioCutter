package com.example.audiocutter.functions.audiochooser.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.audiocutter.R
import com.example.audiocutter.functions.audiochooser.objects.TypeAudioSetAs

class SetAsDialog(val mContext: Context) : DialogFragment(), View.OnClickListener {
    private lateinit var rootView: View
    var typeSet: TypeAudioSetAs? = null


    lateinit var mCallback: setAsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogGray)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.cutchooser_setas_dialog, container, false)
        initViews()
        return rootView
    }

    fun setOnCallBack(event: setAsListener) {
        mCallback = event
    }

    private fun initViews() {

        rootView.findViewById<TextView>(R.id.tv_cancel_dialog_set_as).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.tv_set_dialog_set_as).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.rb_alarm).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.rb_ringtone).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.rb_notification).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.rb_contact).setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.rb_alarm -> typeSet = TypeAudioSetAs.ALARM

            R.id.rb_notification -> typeSet = TypeAudioSetAs.NOTIFICATION

            R.id.rb_ringtone -> typeSet = TypeAudioSetAs.RINGTONE

            //handle
            R.id.rb_contact -> typeSet = TypeAudioSetAs.CONTACT

            R.id.tv_cancel_dialog_set_as -> dismiss()

            R.id.tv_set_dialog_set_as -> setAsAudioFile()
        }
    }

    private fun setAsAudioFile() {
        if (typeSet == null) {
            Toast.makeText(context, "please enter choose", Toast.LENGTH_SHORT).show()
        } else {
            mCallback.setAsTypeAudio(typeSet!!)
        }
    }

    interface setAsListener {
        fun setAsTypeAudio(typeAudioSetAs: TypeAudioSetAs)
    }

}

