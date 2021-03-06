package com.example.audiocutter.functions.mystudio

object Constance {
    val MIN_DURATION = 1000
    val ACTION_DELETE_STATUS = "ACTION_UNCHECK"
    val ACTION_HIDE = "ACTION_HIDE"
    val ACTION_DELETE_ALL = "ACTION_DELETE_ALL"
    val ACTION_STOP_MUSIC = "STOP_MUSIC"
    val ACTION_CHECK_DELETE = "ACTION_CHECK_DELETE"
    val ACTION_DELETE = "ACTION_DELETE"
    val ACTION_SHARE = "ACTION_SHARE"
    val ACTION_SORT_AUDIO = "ACTION_SORT_AUDIO"

    val TYPE_AUDIO_TO_NOTIFICATION = "TYPE_AUDIO_TO_NOTIFICATION"
    val TIME_LIFE_DEFAULT = "00:00"
    val RINGTONE_TYPE = 0
    val ALARM_TYPE = 1
    val NOTIFICATION_TYPE = 2
    val CONTACT_TYPE = 3

    val AUDIO_CUTTER = 0
    val AUDIO_MERGER = 1
    val AUDIO_MIXER = 2

    val NO_TYPE_AUDIO = -1
    val TRUE = 3
    val FALSE = 4

    val AUDIO_CUTTER_STRING = "Audio Cutter"
    val AUDIO_MERGER_STRING = "Audio Merger"
    val AUDIO_MIXER_STRING = "Audio Mixer"

    val TYPE_RESULT = "TYPE_RESULT"
    val TYPE_AUDIO = "TYPE_AUDIO"
    val NOTIFICATION_ACTION_EDITOR_CONVERTING = "NOTIFICATION_ACTION_EDITOR_CONVERTING"
    val NOTIFICATION_ACTION_EDITOR_COMPLETE_CUT = "NOTIFICATION_ACTION_EDITOR_COMPLETE_CUT"
    val NOTIFICATION_ACTION_EDITOR_COMPLETE_MIX = "NOTIFICATION_ACTION_EDITOR_COMPLETE_MIX"
    val NOTIFICATION_ACTION_EDITOR_COMPLETE_MERGER = "NOTIFICATION_ACTION_EDITOR_COMPLETE_MERGER"
    val NOTIFICATION_ACTION_EDITOR_FAIL = "NOTIFICATION_ACTION_EDITOR_FAIL"
    val TYPE_RESULT_CONVERTING = "TYPE_RESULT_CONVERTING"
    val TYPE_RESULT_COMPLETE_CUT = "TYPE_RESULT_COMPLETE_CUT"
    val TYPE_RESULT_COMPLETE_MIX = "TYPE_RESULT_COMPLETE_MIX"
    val TYPE_RESULT_COMPLETE_MERGER = "TYPE_RESULT_COMPLETE_MERGER"
    val TYPE_RESULT_FAIL = "TYPE_RESULT_FAIL"
    val MP3 = ".mp3"
    val M4A = ".m4a"
    val AAC = ".aac"

    val SERVICE_ACTION_BUILD_FORGROUND_SERVICE = "SERVICE_ACTION_BUILD_FORGROUND_SERVICE"
    val SERVICE_ACTION_CANCEL_NOTIFICATION = "SERVICE_ACTION_CANCEL_NOTIFICATION"
    val SERVICE_ACTION_REFESHER_NOTIFICATION ="SERVICE_ACTION_REFESHER_NOTIFICATION"
    val SERVICE_ACTION_CANCEL_ID = "SERVICE_ACTION_CANCEL_ID"
    val SERVICE_ACTION_CHANGE_LANGUAGE = "SERVICE_ACTION_CHANGE_LANGUAGE"

    const val PERMISSION_CONTACTS_SCREEN = 10001
    const val PERMISSION_CUT_SCREEN = 10002
    const val PERMISSION_MIX_SCREEN = 10003
    const val PERMISSION_MERGER_SCREEN = 10004
    const val PERMISSION_MY_STUDIO = 10005
    const val PERMISSION_SET_CONTACT = 10006
    const val PERMISSION_MAIN = 10007

}