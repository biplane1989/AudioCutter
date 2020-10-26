//package com.example.audiocutter.activities.acttest
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import androidx.appcompat.app.AppCompatActivity
//import com.example.audiocutter.R
//import com.example.audiocutter.core.manager.ManagerFactory
//import com.example.audiocutter.functions.resultscreen.objects.CuttingConfig
//import com.example.audiocutter.functions.resultscreen.objects.MergingConfig
//import com.example.audiocutter.functions.resultscreen.objects.MixingConfig
//import com.example.audiocutter.objects.AudioFile
//import com.example.core.core.AudioFormat
//import java.io.File
//
//
//class ResultTestActivity : AppCompatActivity() {
//
//    companion object {
//        const val AUDIO_FILE_PATH_KEY1 = "AUDIO_FILE_PATH_KEY1"
//        const val AUDIO_FILE_PATH_KEY2 = "AUDIO_FILE_PATH_KEY2"
//        fun startActivity(context: Context, audioPath1: String, audioPath2: String) {
//            val intent = Intent(context, ResultTestActivity::class.java)
//            intent.putExtra(AUDIO_FILE_PATH_KEY1, audioPath1)
//            intent.putExtra(AUDIO_FILE_PATH_KEY2, audioPath2)
//            context.startActivity(intent)
//        }
//    }
//
//    val TAG = "giangtd"
//    val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/lonely.mp3")
//    val audioFile = AudioFile(file, "T o m a t o", 100000, 128, uri = Uri.parse(file.absolutePath))
//
//    //    val audioFile = ManagerFactory.getAudioFileManager().buildAudioFile(file.absolutePath)
//    val cuttingConfig = CuttingConfig(1)
//    val mixConfig = MixingConfig(1)
//    val merConfig = MergingConfig(AudioFormat.MP3)
//
//    val listAudio = ArrayList<AudioFile>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_result_test)
//        val audioPath1 = intent.getStringExtra(AUDIO_FILE_PATH_KEY1)!!
//        val audioPath2 = intent.getStringExtra(AUDIO_FILE_PATH_KEY2)!!
//        val audioFile1 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath1)
//        val audioFile2 = ManagerFactory.getAudioFileManager().buildAudioFile(audioPath2)
//        val mergingConfig = MergingConfig(AudioFormat.MP3)
//        val outFile = File(Environment.getExternalStorageDirectory()
//            .toString() + "/Music/apple")
//        val output = AudioFile(outFile, outFile.name, file.length())
//
//        ManagerFactory.getAudioEditorManager()
//            .mergeAudio(arrayListOf(audioFile1, audioFile2), mergingConfig, output)
//
//        /* listAudio.add(audioFile)
//         listAudio.add(audioFile)*/
//
//        /*   btn_result.setOnClickListener(View.OnClickListener {
//
//               ManagerFactory.getAudioEditorManager().cutAudio(audioFile, cuttingConfig, file)
//               val intent = Intent(this, ResultActivity::class.java)
//               startActivity(intent)
//           })
//
//           btn_mix.setOnClickListener(View.OnClickListener {
//               ManagerFactory.getAudioEditorManager()
//                   .mixAudio(audioFile, audioFile, mixConfig, audioFile)
//               val intent = Intent(this, ResultActivity::class.java)
//               startActivity(intent)
//           })
//
//           btn_mer.setOnClickListener(View.OnClickListener {
//               ManagerFactory.getAudioEditorManager().mergeAudio(listAudio, merConfig, audioFile)
//               val intent = Intent(this, ResultActivity::class.java)
//               startActivity(intent)
//           })
//   */
//
//    }
//
//}