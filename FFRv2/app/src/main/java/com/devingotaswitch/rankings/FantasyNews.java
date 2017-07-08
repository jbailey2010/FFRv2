package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyNews extends AppCompatActivity {

    private Rankings rankings;

    private static final String RW_HEADLINE_TITLE = "Rotoworld Headline News";
    private static final String RW_PLAYER_TITLE = "Rotoworld Player News";
    private static final String MFL_AGGREGATE_TITLE = "MFL Aggregate News";

    private static final String TAG = "ParseNews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fantasy_news);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_player_news);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Fantasy News");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init();
    }

    private void init() {
        List<String> sources = new ArrayList<>();
        sources.add(RW_HEADLINE_TITLE);
        sources.add(RW_PLAYER_TITLE);
        sources.add(MFL_AGGREGATE_TITLE);
        final Spinner sourcesSpinner = (Spinner)findViewById(R.id.news_source_selector);
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sources);
        sourcesSpinner.setAdapter(teamAdapter);

        Button submit = (Button)findViewById(R.id.news_selection_submit);
        final Context localCopy = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.confirmInternet(localCopy)) {
                    String selectedSource = ((TextView)sourcesSpinner.getSelectedView()).getText().toString();
                    getNews(selectedSource);
                } else {
                    Toast.makeText(localCopy, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getNews(String source) {
        ParseNews news = new ParseNews(this, source);
        news.execute();
    }

    private void displayNews(List<PlayerNews> news) {
        ListView listview = (ListView) findViewById(R.id.news_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status });
        listview.setAdapter(adapter);
        for (PlayerNews newsItem : news) {
            Map<String, String> datum = new HashMap<>(3);
            datum.put(Constants.PLAYER_BASIC, newsItem.getNews());
            datum.put(Constants.PLAYER_INFO, new StringBuilder(newsItem.getImpact())
                    .append(Constants.LINE_BREAK)
                    .append(Constants.LINE_BREAK)
                    .append(newsItem.getDate())
                    .toString());
            data.add(datum);
        }
        adapter.notifyDataSetChanged();
    }

    class ParseNews extends AsyncTask<Object, Void, List<PlayerNews>> {
        ProgressDialog pdia;
        FantasyNews act;
        String source;
        public ParseNews(FantasyNews act, String source) {
            pdia = new ProgressDialog(act);
            pdia.setCancelable(false);
            this.act = act;
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
                if (RW_PLAYER_TITLE.equals(source)) {
                    news = parseNewsRoto("http://www.rotoworld.com/playernews/nfl/football-player-news");
                } else if (RW_HEADLINE_TITLE.equals(source)) {
                    news = parseNewsRoto("http://www.rotoworld.com/headlines/nfl/0/football-headlines");
                } else if (MFL_AGGREGATE_TITLE.equals(source)) {
                    news = parseMFL();
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
        List<String> reportSet = JsoupUtils.handleListsMulti(doc, "div.report");
        List<String> impactSet = JsoupUtils.handleListsMulti(doc, "div.impact");
        List<String> dateSet = JsoupUtils.handleListsMulti(doc, "div.date");
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
        String url = "http://football.myfantasyleague.com/2017/news_articles";
        Document doc = Jsoup.connect(url).timeout(0).get();
        List<String> title = JsoupUtils.handleListsMulti(doc, "td.headline b a");
        Elements elems = doc.select("tr.oddtablerow");
        Elements elems2 = doc.select("tr.eventablerow");
        List<String> news = new ArrayList<String>();
        for(int i = 0; i < elems.size(); i++){
            Element odd = elems.get(i).child(2);
            Element even= elems2.get(i).child(2);
            news.add(odd.text());
            news.add(even.text());
        }
        List<String> time = JsoupUtils.handleListsMulti(doc, "td.timestamp");
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
