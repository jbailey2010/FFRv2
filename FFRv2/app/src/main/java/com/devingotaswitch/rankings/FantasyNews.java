package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
        final List<String> sources = new ArrayList<>(Arrays.asList(Constants.MFL_AGGREGATE_TITLE,
                Constants.SPOTRAC_TRANSACTIONS_TITLE, Constants.FP_ALL_NEWS,
                Constants.FP_RUMORS_TITLE, Constants.FP_BREAKING_NEWS_TITLE, Constants.FP_INJURY_TITLE));
        final NiceSpinner sourcesSpinner = findViewById(R.id.news_source_selector);

        sourcesSpinner.attachDataSource(sources);
        sourcesSpinner.setBackgroundColor(Color.parseColor("#FAFAFA"));
        sourcesSpinner.setSelectedIndex(sources.indexOf(LocalSettingsHelper.getSelectedNewsSource(this)));

        final Button submit = findViewById(R.id.news_selection_submit);
        final Activity localCopy = this;
        submit.setOnClickListener(v -> {
            if (GeneralUtils.confirmInternet(localCopy)) {
                String selectedSource = sources.get(sourcesSpinner.getSelectedIndex());
                getNews(selectedSource);
                LocalSettingsHelper.saveSelectedNewsSource(localCopy, selectedSource);
            } else {
                FlashbarFactory.generateTextOnlyFlashbar(localCopy, "No can do", "No internet connection available",
                        Flashbar.Gravity.BOTTOM)
                        .show();
            }
        });

        List<PlayerNews> cachedNews = LocalSettingsHelper.loadNews(this);
        if (cachedNews.size() > 0) {
            displayNews(cachedNews);
        }
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
        adapter.setOnItemClickListener((view, position) -> {
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
        });
        listview.setLayoutManager(new LinearLayoutManager(this));
        listview.addItemDecoration(DisplayUtils.getVerticalDividerDecoration(this));

        listview.setAdapter(adapter);
        findViewById(R.id.main_toolbar_title).setOnClickListener(v -> listview.smoothScrollToPosition(0));

        LocalSettingsHelper.cacheNews(this, news);
    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
    }

    class ParseNews extends AsyncTask<Object, Void, List<PlayerNews>> {
        final AlertDialog pdia;
        final String source;
        ParseNews(FantasyNews act, String source) {
            pdia = new MaterialAlertDialogBuilder(act)
                .setTitle("Please wait")
                .setMessage("Fetching the news...")
                .setCancelable(false)
                .create();
            this.source = source;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
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
                    case Constants.FP_ALL_NEWS:
                        news = parseFantasyPros("https://www.fantasypros.com/nfl/player-news.php");
                        break;
                    case Constants.FP_BREAKING_NEWS_TITLE:
                        news = parseFantasyPros("https://www.fantasypros.com/nfl/breaking-news.php");
                        break;
                    case Constants.FP_RUMORS_TITLE:
                        news = parseFantasyPros("https://www.fantasypros.com/nfl/rumors.php");
                        break;
                    case Constants.FP_INJURY_TITLE:
                        news = parseFantasyPros("https://www.fantasypros.com/nfl/injury-news.php");
                        break;
                    case Constants.MFL_AGGREGATE_TITLE:
                        news = parseMFL();
                        break;
                    case Constants.SPOTRAC_TRANSACTIONS_TITLE:
                        news = parseSpotrac();
                        break;
                }
                return news;
            } catch (Exception e) {
                Log.e(TAG, "Failed to get news", e);
            }
            return null;
        }
    }

    private List<PlayerNews> parseSpotrac() throws IOException {
        List<PlayerNews> newsSet = new ArrayList<>();
        Document doc = Jsoup.connect("https://www.spotrac.com/nfl/transactions/").timeout(0).get();
        List<String> dates = JsoupUtils.getElemsFromDoc(doc, "span.date");
        List<String> headlines = JsoupUtils.getElemsFromDoc(doc, "div.transactions div#transactionslist div.cnt a");
        List<String> content = JsoupUtils.getElemsFromDoc(doc, "div.transactions div#transactionslist div.cnt p");
        for (int i = 0; i < dates.size(); i++) {
            PlayerNews newsItem = new PlayerNews();
            newsItem.setDate(dates.get(i));
            newsItem.setNews(headlines.get(i));
            newsItem.setImpact(content.get(i));
            newsSet.add(newsItem);
        }
        return newsSet;
    }

    private List<PlayerNews> parseFantasyPros(String url) throws IOException {
        List<PlayerNews> newsSet = new ArrayList<>();
        Document doc = Jsoup.connect(url).timeout(0).get();
        List<String> reportSet = JsoupUtils.getElemsFromDoc(doc, "div.player-news-header div.ten a");
        Elements links = doc.select("div.ten p");
        List<String> impactSet = new ArrayList<>();
        for (Element element : links) {
            if (!element.text().startsWith("Category:")) {
                impactSet.add(element.text());
            }
        }
        List<String> dateSet = JsoupUtils.getElemsFromDoc(doc, "div.player-news-header div.ten p");
        for(int i = 0; i < dateSet.size(); i++)
        {
            PlayerNews news = new PlayerNews();
            news.setNews(reportSet.get(i * 2));
            String impact = impactSet.get(i * 3 + 1) +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    impactSet.get(i * 3 + 2);
            news.setImpact(impact);
            news.setDate(dateSet.get(i).split(" By ")[0]);
            newsSet.add(news);
        }
        return newsSet;
    }

    private List<PlayerNews> parseMFL() throws IOException {
        List<PlayerNews> newsSet = new ArrayList<>();
        String url = "http://www03.myfantasyleague.com/" + Constants.YEAR_KEY + "/news_articles?L=&PLAYERS=*&SOURCE=*&TEAM=*&POSITION=*&DAYS=7";
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
