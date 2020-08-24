package com.devingotaswitch.utils

import android.app.Activity
import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.utils.GeneralUtils.isInteger

object DraftUtils {
    @JvmStatic
    fun getUndraftListener(activity: Activity?, rankings: Rankings, player: Player,
                           adapter: RecyclerViewAdapter, data: MutableList<MutableMap<String, String?>>, datum: MutableMap<String, String?>,
                           position: Int, updateList: Boolean): OnActionTapListener {
        return object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                rankings.draft.undraft(rankings, player, activity)
                if (updateList) {
                    data.add(position, datum)
                } else {
                    datum[Constants.PLAYER_ADDITIONAL_INFO] = ""
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    @JvmStatic
    fun getAuctionCostDialog(activity: Activity?, player: Player, callback: AuctionCostInterface): AlertDialog {
        val li = LayoutInflater.from(activity)
        val noteView = li.inflate(R.layout.user_input_popup, null)
        val alertDialogBuilder = AlertDialog.Builder(
                activity!!)
        alertDialogBuilder.setView(noteView)
        val userInput = noteView
                .findViewById<EditText>(R.id.user_input_popup_input)
        userInput.inputType = InputType.TYPE_CLASS_NUMBER
        userInput.hint = "Auction cost"
        val title = noteView.findViewById<TextView>(R.id.user_input_popup_title)
        title.text = "How much did " + player.name + " cost?"
        alertDialogBuilder
                .setPositiveButton("Save") { _: DialogInterface?, _: Int ->
                    val input = userInput.text.toString()
                    if (input.isEmpty() || !isInteger(input)) {
                        callback.onInvalidInput()
                    } else {
                        callback.onValidInput(input.toInt())
                    }
                }
                .setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> callback.onCancel() }
        alertDialogBuilder.setCancelable(false)
        return alertDialogBuilder.create()
    }

    interface AuctionCostInterface {
        fun onValidInput(cost: Int?)
        fun onInvalidInput()
        fun onCancel()
    }
}