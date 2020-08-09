package com.devingotaswitch.utils;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter;

import java.util.List;
import java.util.Map;

public class DraftUtils {

    public static Flashbar.OnActionTapListener getUndraftListener(final Activity activity, final Rankings rankings, final Player player,
                                                                  final View view, final RecyclerViewAdapter adapter, final List<Map<String, String>> data,
                                                                  final Map<String, String> datum, final int position, final boolean updateList) {
        return new Flashbar.OnActionTapListener() {
            @Override
            public void onActionTapped(Flashbar flashbar) {
                rankings.getDraft().undraft(rankings, player, activity, view);
                if (updateList) {
                    data.add(position, datum);
                    adapter.notifyDataSetChanged();
                } else {
                    datum.put(Constants.PLAYER_ADDITIONAL_INFO, "");
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    public static AlertDialog getAuctionCostDialog(Activity activity, Player player, final AuctionCostInterface callback) {
        LayoutInflater li = LayoutInflater.from(activity);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        alertDialogBuilder.setView(noteView);
        final EditText userInput =  noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        userInput.setHint("Auction cost");

        TextView title = noteView.findViewById(R.id.user_input_popup_title);
        title.setText("How much did " + player.getName() + " cost?");
        alertDialogBuilder
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String input = userInput.getText().toString();
                        if (input.isEmpty() || !GeneralUtils.isInteger(input)) {
                            callback.onInvalidInput();
                        } else {
                            callback.onValidInput(Integer.parseInt(input));
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onCancel();
                    }
                });
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    public interface AuctionCostInterface {
        void onValidInput(Integer cost);

        void onInvalidInput();

        void onCancel();
    }
}
