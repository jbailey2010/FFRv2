package com.devingotaswitch.rankings

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import au.com.bytecode.opencsv.CSVWriter
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class ExportRankings : AppCompatActivity() {
    private lateinit var rankings: Rankings
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_rankings)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()
        val toolbar = findViewById<Toolbar>(R.id.export_rankings_toolbar)
        toolbar.title = ""
        val main_title = findViewById<TextView>(R.id.main_toolbar_title)
        main_title.text = "Export Rankings"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
    }

    public override fun onResume() {
        super.onResume()
        try {
            init()
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            onBackPressed()
        }
    }

    private fun init() {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val submit = findViewById<Button>(R.id.export_rankings_button)
        submit.setOnClickListener { v: View? -> requestExportPermissions() }
    }

    private fun requestExportPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1)
        } else {
            exportRankings()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportRankings()
        } else {
            generateTextOnlyFlashbar(this, "No can do", "Can't export rankings without permission", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    private fun exportRankings() {
        try {
            val context = applicationContext
            val fileLocation = File(getExternalFilesDir(null), FILE_NAME)
            val path: Uri = FileProvider.getUriForFile(context, context.packageName,
                    fileLocation)
            saveCSV(fileLocation)
            Log.d(TAG, path.toString())
            val share = Intent(Intent.ACTION_SEND)
            share.putExtra(Intent.EXTRA_STREAM, path)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.type = "text/csv"

            val chooser = Intent.createChooser(share, "Export Rankings")

            val resInfoList: List<ResolveInfo> = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName: String = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export rankings", e)
            generateTextOnlyFlashbar(this, "No can do", "Failed to export", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    @Throws(IOException::class)
    private fun saveCSV(file: File) {
        val data = arrayOf("Name", "Age", "Position", "Team Name", "Positional SOS", "Bye Week", "ECR", "ADP", "Dynasty/Keeper Rank",
                "Rookie Rank", "Best Ball Rank", "$200 Unscaled Auction Value", "Customized Auction Value", "Projection", "PAA", "XVal",
                "vOLS", "Note", "Watched")
        val writer = CSVWriter(FileWriter(file))
        Log.d(TAG, "Logging to $file")
        writer.writeNext(data)
        for (key in rankings.orderedIds) {
            val player = rankings.getPlayer(key)
            if (rankings.leagueSettings.rosterSettings!!.isPositionValid(player.position)) {
                val playerData: MutableList<String> = ArrayList()
                playerData.add(player.name)
                playerData.add(if (player.age != null && player.age > 0) { player.age.toString() } else { "" })
                playerData.add(player.position)
                playerData.add(player.teamName)
                val team = rankings.getTeam(player)
                if (team != null) {
                    playerData.add(if (!StringUtils.isBlank(player.position)) team.getSosForPosition(player.position).toString() else "")
                    playerData.add(team.bye.toString())
                } else {
                    playerData.add("")
                    playerData.add("")
                }
                playerData.add(player.ecr.toString())
                playerData.add(player.adp.toString())
                playerData.add(player.dynastyRank.toString())
                playerData.add(player.rookieRank.toString())
                playerData.add(player.bestBallRank.toString())
                playerData.add(player.auctionValue.toString())
                playerData.add(player.getAuctionValueCustom(rankings).toString())
                playerData.add(player.projection.toString())
                playerData.add(player.paa.toString())
                playerData.add(player.getxVal().toString())
                playerData.add(player.vols.toString())
                playerData.add(rankings.getPlayerNote(player.uniqueId))
                playerData.add(rankings.isPlayerWatched(player.uniqueId).toString())

                // Allocating at size 0 is faster than the actual size (somehow)
                writer.writeNext(playerData.toTypedArray())
            }
        }
        writer.close()
    }

    companion object {
        private const val FILE_NAME = "rankings.csv"
        private const val TAG = "ExportRankings"
    }
}