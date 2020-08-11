package com.devingotaswitch.rankings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.FlashbarFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class ExportRankings extends AppCompatActivity {

    private Rankings rankings;

    private static final String FILE_NAME = "rankings.csv";
    private static final String TAG = "ExportRankings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_rankings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = findViewById(R.id.export_rankings_toolbar);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("Export Rankings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            onBackPressed();
        }
    }

    private void init() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Button submit = findViewById(R.id.export_rankings_button);
        submit.setOnClickListener(v -> requestExportPermissions());
    }

    private void requestExportPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            exportRankings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
       // If request is cancelled, the result arrays are empty.
       if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           exportRankings();
       } else {
           FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Can't export rankings without permission", Flashbar.Gravity.BOTTOM)
                   .show();
       }
    }

    private void exportRankings() {
        try {
            saveCSV();

            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String filePath = baseDir + File.separator + FILE_NAME;
            File file = new File(filePath);
            Uri path = Uri.fromFile(file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, path);
            share.setType("text/csv");
            startActivity(Intent.createChooser(share, "Export Rankings"));


        } catch (Exception e) {
            Log.e(TAG, "Failed to export rankings", e);
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Failed to export", Flashbar.Gravity.BOTTOM)
                    .show();
        }
    }

    private void saveCSV() throws IOException {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath = baseDir + File.separator + FILE_NAME;
        CSVWriter writer = new CSVWriter(new FileWriter(filePath));

        String[] data = {"Name", "Age", "Position", "Team Name", "Positional SOS", "Bye Week", "ECR", "ADP", "Dynasty/Keeper Rank",
                "Rookie Rank", "Best Ball Rank", "$200 Unscaled Auction Value", "Customized Auction Value", "Projection", "PAA", "XVal",
                "vOLS", "Note", "Watched"};
        writer.writeNext(data);
        for (String key : rankings.getOrderedIds()) {
            Player player = rankings.getPlayer(key);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition())) {
                List<String> playerData = new ArrayList<>();
                playerData.add(player.getName());
                playerData.add(player.getAge() > 0 ? String.valueOf(player.getAge()) : "");
                playerData.add(player.getPosition());
                playerData.add(player.getTeamName());
                Team team = rankings.getTeam(player);
                if (team != null) {
                    playerData.add(!StringUtils.isBlank(player.getPosition()) ?
                            String.valueOf(team.getSosForPosition(player.getPosition())) : "");
                    playerData.add(String.valueOf(team.getBye()));
                } else {
                    playerData.add("");
                    playerData.add("");
                }
                playerData.add(String.valueOf(player.getEcr()));
                playerData.add(String.valueOf(player.getAdp()));
                playerData.add(String.valueOf(player.getDynastyRank()));
                playerData.add(String.valueOf(player.getRookieRank()));
                playerData.add(String.valueOf(player.getBestBallRank()));
                playerData.add(String.valueOf(player.getAuctionValue()));
                playerData.add(String.valueOf(player.getAuctionValueCustom(rankings)));
                playerData.add(String.valueOf(player.getProjection()));
                playerData.add(String.valueOf(player.getPaa()));
                playerData.add(String.valueOf(player.getxVal()));
                playerData.add(String.valueOf(player.getVOLS()));
                playerData.add(rankings.getPlayerNote(player.getUniqueId()));
                playerData.add(String.valueOf(rankings.isPlayerWatched(player.getUniqueId())));

                // Allocating at size 0 is faster than the actual size (somehow)
                writer.writeNext(playerData.toArray(new String[0]));
            }
        }

        writer.close();
    }
}
