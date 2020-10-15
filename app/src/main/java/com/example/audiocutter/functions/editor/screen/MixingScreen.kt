package com.example.audiocutter.functions.editor.screen


import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.audiocutter.R
import com.example.audiocutter.base.BaseFragment
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.core.manager.PlayerInfo
import com.example.audiocutter.core.manager.PlayerState
import com.example.audiocutter.databinding.MixingScreenBinding
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.ui.audiochooser.mix.ChangeRangeView
import com.example.audiocutter.util.Utils

class MixingScreen : BaseFragment(), View.OnClickListener, ChangeRangeView.OnPlayLineChange {
    private val TAG = MixingScreen::class.java.name
    private var playerState = PlayerState.IDLE
    private lateinit var binding: MixingScreenBinding
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mixing_screen, container, false)
        initViews()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ManagerFactory.getAudioPlayer().getPlayerInfo().observe(viewLifecycleOwner, observerAudio())
    }


    private fun observerAudio(): Observer<PlayerInfo> {
        return Observer {
            when (it.playerState) {
                PlayerState.IDLE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.IDLE
                }
                PlayerState.PREPARING -> {
                }
                PlayerState.PLAYING -> {
                    binding.crChangeViewMixing.setPosition(it.posision)
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_pause_ic)
                    playerState = PlayerState.PLAYING
                }
                PlayerState.PAUSE -> {
                    binding.playIv.setImageResource(R.drawable.fragment_cutter_play_ic)
                    playerState = PlayerState.PAUSE
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun initViews() {
        binding.lnAddItemMixing.removeAllViews()

        audioFile1 = ManagerFactory.getAudioFileManager()
            .buildAudioFile("/storage/emulated/0/Download/Ed Sheeran - Shape Of You [Official].mp3")
        audioFile2 = ManagerFactory.getAudioFileManager()
            .buildAudioFile("/storage/emulated/0/Download/Cung-Dan-Tinh-Yeu-Dan-Truong-My-Tam.mp3")

        val viewAudioFile1 =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.item_audio_mixing, null)

        val viewAudioFile2 =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.item_audio_mixing, null)


        val tvName1 = viewAudioFile1.findViewById<TextView>(R.id.tv_name_mixing_choose_audio)
        val tvName2 = viewAudioFile2.findViewById<TextView>(R.id.tv_name_mixing_choose_audio)
        tvName1.text = audioFile1.fileName
        tvName2.text = audioFile2.fileName
        binding.lnAddItemMixing.addView(viewAudioFile1)
        binding.lnAddItemMixing.addView(viewAudioFile2)


        binding.crChangeViewMixing.setFileAudio(audioFile1)
        binding.crChangeViewMixing.mCallback = this
        binding.playIv.setOnClickListener(this)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    override fun onClick(v: View) {
        when (v) {
            binding.playIv -> {
                runOnUI {
                    if (playerState == PlayerState.PLAYING) {
                        ManagerFactory.getAudioPlayer().pause()
                    } else {
                        if (playerState == PlayerState.IDLE)
                            ManagerFactory.getAudioPlayer().play(audioFile1)
                        else ManagerFactory.getAudioPlayer().resume()
                    }
                }
            }
        }
    }

    override fun onLineChange(audioFile: AudioFile, pos: Int) {
        runOnUI {
            ManagerFactory.getAudioPlayer().play(audioFile, pos)
        }
    }

    override fun pauseInvalid() {
        ManagerFactory.getAudioPlayer().pause()
    }
}