package com.example.audiocutter.functions.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils

enum class SortField {
    SORT_BY_NAME,
    SORT_BY_DATE,
    SORT_BY_DURATION,
    SORT_BY_SIZE
}

enum class SortType {
    ASC,
    DESC
}

data class SortValue(val sortType: SortType, val sortField: SortField)

class SortAudioPopupWindow(
    val view: View,
    val defaultSortValue: SortValue,
    val onItemSelected: (sortValue: SortValue) -> Unit
) : View.OnClickListener {
    companion object {
        val UNSELECT_ITEM_COLOR = Color.parseColor("#FFFFFF")
        val SELECT_ITEM_COLOR = Color.parseColor("#1AFCB674")
        val SELECT_TEXT_BUTTON_COLOR = Color.parseColor("#FFFFFF")
        val UNSELECT_TEXT_BUTTON_COLOR = Color.parseColor("#9C9C9C")
    }

    private var sortFieldSelected = defaultSortValue.sortField

    private val sortByNameButton: TextView
    private val sortByDateButton: TextView
    private val sortByDurationButton: TextView
    private val sortBySizeButton: TextView
    private val sortLongestButton: TextView
    private val sortShortestButton: TextView
    private val popupView: View
    private val popupWindow: PopupWindow

    init {
        popupView = LayoutInflater.from(view.context)
            .inflate(R.layout.menu_sort_audio, null)
        sortLongestButton = popupView.findViewById(R.id.longest_button)
        sortShortestButton = popupView.findViewById(R.id.shortest_button)

        sortByNameButton = popupView.findViewById(R.id.sort_by_name_button)
        sortByDateButton = popupView.findViewById(R.id.sort_by_date_button)
        sortByDurationButton = popupView.findViewById(R.id.sort_by_duration_button)
        sortBySizeButton = popupView.findViewById(R.id.sort_by_size_button)
        popupWindow = PopupWindow(
            popupView,
            Utils.dpToPx(view.context, 240f),
            Utils.dpToPx(view.context, 300f),
            true
        )
    }

    fun show() {
        selectSortType()
        selectSortField()
        bindEvents()
        popupWindow.showAsDropDown(
            view,
            -Utils.dpToPx(view.context, 150f),
            -Utils.dpToPx(view.context, 10f)
        )
    }

    private fun bindEvents() {
        sortByNameButton.setOnClickListener(this)
        sortByDateButton.setOnClickListener(this)
        sortByDurationButton.setOnClickListener(this)
        sortBySizeButton.setOnClickListener(this)

        sortLongestButton.setOnClickListener(this)
        sortShortestButton.setOnClickListener(this)
    }

    private fun selectSortType() {
        when (defaultSortValue.sortType) {
            SortType.ASC -> {
                sortLongestButton.setBackgroundResource(R.drawable.menu_sort_audio_bg_selected_sort_button)
                sortLongestButton.setTextColor(SELECT_TEXT_BUTTON_COLOR)

                sortShortestButton.setBackgroundResource(R.drawable.menu_sort_audio_bg_unselected_sort_button)
                sortShortestButton.setTextColor(UNSELECT_TEXT_BUTTON_COLOR)

            }
            SortType.DESC -> {
                sortLongestButton.setBackgroundResource(R.drawable.menu_sort_audio_bg_unselected_sort_button)
                sortLongestButton.setTextColor(UNSELECT_TEXT_BUTTON_COLOR)

                sortShortestButton.setBackgroundResource(R.drawable.menu_sort_audio_bg_selected_sort_button)
                sortShortestButton.setTextColor(SELECT_TEXT_BUTTON_COLOR)

            }
        }

    }

    private fun selectSortField() {
        when (defaultSortValue.sortField) {
            SortField.SORT_BY_NAME -> {
                sortByNameButton.setBackgroundColor(SELECT_ITEM_COLOR)
            }
            SortField.SORT_BY_DATE -> {
                sortByDateButton.setBackgroundColor(SELECT_ITEM_COLOR)
            }
            SortField.SORT_BY_DURATION -> {
                sortByDurationButton.setBackgroundColor(SELECT_ITEM_COLOR)
            }
            SortField.SORT_BY_SIZE -> {
                sortBySizeButton.setBackgroundColor(SELECT_ITEM_COLOR)
            }
        }
    }

    private fun unselectAllItems() {
        sortByNameButton.setBackgroundColor(UNSELECT_ITEM_COLOR)
        sortByDateButton.setBackgroundColor(UNSELECT_ITEM_COLOR)
        sortByDurationButton.setBackgroundColor(UNSELECT_ITEM_COLOR)
        sortBySizeButton.setBackgroundColor(UNSELECT_ITEM_COLOR)
    }

    override fun onClick(view: View) {
        when (view) {
            sortByNameButton -> {
                unselectAllItems()
                sortByNameButton.setBackgroundColor(SELECT_ITEM_COLOR)
                sortFieldSelected = SortField.SORT_BY_NAME
            }
            sortByDateButton -> {
                unselectAllItems()
                sortByDateButton.setBackgroundColor(SELECT_ITEM_COLOR)
                sortFieldSelected = SortField.SORT_BY_DATE
            }
            sortByDurationButton -> {
                unselectAllItems()
                sortByDurationButton.setBackgroundColor(SELECT_ITEM_COLOR)
                sortFieldSelected = SortField.SORT_BY_DURATION
            }
            sortBySizeButton -> {
                unselectAllItems()
                sortBySizeButton.setBackgroundColor(SELECT_ITEM_COLOR)
                sortFieldSelected = SortField.SORT_BY_SIZE
            }

            sortLongestButton -> {
                onItemSelected(SortValue(SortType.ASC, sortFieldSelected))
                popupWindow.dismiss()
            }

            sortShortestButton -> {
                onItemSelected(SortValue(SortType.DESC, sortFieldSelected))
                popupWindow.dismiss()
            }
        }
    }
}