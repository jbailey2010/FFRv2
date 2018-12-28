package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DisplayUtils;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

import org.angmarch.views.NiceSpinner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyNews extends AppCompatActivity {

    private Rankings rankings;

    private static final String TAG = "ParseNews";

    private static Map<String, String> nameToId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fantasy_news);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = findViewById(R.id.toolbar_player_news);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("News");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            init();
        } catch(Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            onBackPressed();
        }
    }

    private void init() {
        nameToId = new HashMap<>();
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            nameToId.put(player.getName(), key);
        }
        final List<String> sources = new ArrayList<>(Arrays.asList(Constants.RW_HEADLINE_TITLE, Constants.RW_PLAYER_TITLE, Constants.MFL_AGGREGATE_TITLE));
        final NiceSpinner sourcesSpinner = findViewById(R.id.news_source_selector);

        sourcesSpinner.attachDataSource(sources);
        sourcesSpinner.setBackgroundColor(Color.parseColor("#FAFAFA"));
        sourcesSpinner.setSelectedIndex(sources.indexOf(LocalSettingsHelper.getSelectedNewsSource(this)));

        final Button submit = findViewById(R.id.news_selection_submit);
        final Activity localCopy = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.confirmInternet(localCopy)) {
                    String selectedSource = sources.get(sourcesSpinner.getSelectedIndex());
                    getNews(selectedSource);
                    LocalSettingsHelper.saveSelectedNewsSource(localCopy, selectedSource);
                } else {
                    FlashbarFactory.generateTextOnlyFlashbar(localCopy, "No can do", "No internet connection available",
                            Flashbar.Gravity.BOTTOM)
                            .show();
                }
            }
        });
    }

    private void getNews(String source) {
        ParseNews news = new ParseNews(this, source);
        news.execute();
    }

    private void displayNews(List<PlayerNews> news) {
        final RecyclerView listview = findViewById(R.id.news_list);
        final List<Map<String, String>> data = new ArrayList<>();
        for (PlayerNews newsItem : news) {
            Map<String, String> datum = new HashMap<>(3);
            datum.put(Constants.PLAYER_BASIC, newsItem.getNews());
            datum.put(Constants.PLAYER_INFO, newsItem.getImpact() +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    newsItem.getDate());
            data.add(datum);
        }
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO},
                new int[] { R.id.player_basic, R.id.player_info});
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (nameToId.size() > 0) {
                    String[] newsMainArr = ((TextView) view.findViewById(R.id.player_basic)).getText().toString()
                            .replaceAll(":", "").replaceAll(",", "").replaceAll("\\?", "").split(" ");
                    for (int i = 0; i < newsMainArr.length - 1; i++) {
                        String possibleName = newsMainArr[i] +
                                " " +
                                newsMainArr[i + 1];
                        if (nameToId.containsKey(possibleName)) {
                            displayPlayerInfo(nameToId.get(possibleName));
                        }
                    }
                }
            }
        });
        listview.setLayoutManager(new LinearLayoutManager(this));
        listview.addItemDecoration(DisplayUtils.getVerticalDividerDecoration(this));

        listview.setAdapter(adapter);
        findViewById(R.id.main_toolbar_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.smoothScrollToPosition(0);
            }
        });


    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
    }

    class ParseNews extends AsyncTask<Object, Void, List<PlayerNews>> {
        final ProgressDialog pdia;
        final String source;
        ParseNews(FantasyNews act, String source) {
            pdia = new ProgressDialog(act);
            pdia.setCancelable(false);
            this.source = source;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia.setMessage("Please wait, fetching the news...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(List<PlayerNews> result){
            super.onPostExecute(result);
            pdia.dismiss();
            displayNews(result);
        }

        @Override
        protected List<PlayerNews> doInBackground(Object... data) {
            List<PlayerNews> news = null;
            try {
                switch (source) {
                    case Constants.RW_PLAYER_TITLE:
                        news = parseNewsRoto("http://www.rotoworld.com/playernews/nfl/football-player-news");
                        break;
                    case Constants.RW_HEADLINE_TITLE:
                        news = parseNewsRoto("http://www.rotoworld.com/headlines/nfl/0/football-headlines");
                        break;
                    case Constants.MFL_AGGREGATE_TITLE:
                        news = parseMFL();
                        break;
                }
                return news;
            } catch (Exception e) {
                Log.e(TAG, "Failed to get news", e);
            }
            return null;
        }
    }

    private List<PlayerNews> parseNewsRoto(String url) throws IOException {
        List<PlayerNews> newsSet = new ArrayList<>();
        Document doc = Jsoup.connect(url).timeout(0).get();
        List<String> reportSet = JsoupUtils.getElemsFromDoc(doc, "div.report");
        List<String> impactSet = JsoupUtils.getElemsFromDoc(doc, "div.impact");
        List<String> dateSet = JsoupUtils.getElemsFromDoc(doc, "div.date");
        for(int i = 0; i < reportSet.size(); i++)
        {
            PlayerNews news = new PlayerNews();
            news.setNews(reportSet.get(i));
            news.setImpact(impactSet.get(i+1));
            news.setDate(dateSet.get(i));
            newsSet.add(news);
        }
        return newsSet;
    }

    private List<PlayerNews> parseMFL() throws IOException {
        List<PlayerNews> newsSet = new ArrayList<>();
        String url = "http://www03.myfantasyleague.com/" + Constants.YEAR_KEY + "/news_articles";
        Document doc = Jsoup.connect(url).timeout(0).get();
        List<String> title = JsoupUtils.getElemsFromDoc(doc, "td.headline b a");
        Elements elems = doc.select("tr.oddtablerow");
        Elements elems2 = doc.select("tr.eventablerow");
        List<String> news = new ArrayList<>();
        for(int i = 0; i < elems.size(); i++){
            Element odd = elems.get(i).child(2);
            Element even= elems2.get(i).child(2);
            news.add(odd.text());
            news.add(even.text());
        }
        List<String> time = JsoupUtils.getElemsFromDoc(doc, "td.timestamp");
        for(int i = 0; i < 75; i++){
            String newsStr = news.get(i);
            newsStr = newsStr.substring(newsStr.indexOf(")") + 2);
            if(newsStr.contains("... (More)")){
                newsStr = newsStr.split("\\(More\\)")[0];
            }
            PlayerNews newsObj = new PlayerNews();
            newsObj.setNews(title.get(i));
            newsObj.setImpact(newsStr);
            newsObj.setDate(time.get(i) + " ago");
            newsSet.add(newsObj);
        }
        return newsSet;
    }
}
