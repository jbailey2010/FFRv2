package com.devingotaswitch.rankings.sources;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParsePlayerNews {


    public static void startNews(String playerName, String playerTeam, PlayerInfo activity) {
        String baseUrl = "http://www.fantasypros.com/nfl/news/"
                + playerNameUrl(playerName, playerTeam) + ".php";
        NewsParser objParse = new NewsParser(activity, baseUrl);
        objParse.execute();
    }

    public static class NewsParser extends
            AsyncTask<Object, String, List<PlayerNews>> {
        PlayerInfo act;
        String urlNews;

        NewsParser(PlayerInfo activity, String url) {
            act = activity;
            urlNews = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<PlayerNews> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                act.populateNews(result);
            }
        }

        @Override
        protected List<PlayerNews> doInBackground(Object... data) {
            List<PlayerNews> newsList = new ArrayList<>();
            try {
                urlNews = urlNews.toLowerCase();
                Document doc = JsoupUtils.getDocument(urlNews);
                Elements noteElems = doc.select("div.body-row div.content");
                for (Element element : noteElems) {
                    String title = element.child(1).text();
                    String body = element.child(3).text();
                    String date = element.parent().parent().child(1).child(1).text();
                    PlayerNews news = new PlayerNews();
                    news.setNews(title);
                    news.setImpact(body + Constants.LINE_BREAK + Constants.LINE_BREAK + date);

                    newsList.add(news);
                }
            } catch (IOException e) {
                return newsList;
            } catch (IndexOutOfBoundsException e) {
                return newsList;
            }
            return newsList;
        }

    }

	private static String playerNameUrl(String playerName, String teamName) {

		if (!playerName.contains(Constants.DST)) {
			String[] nameSet = playerName.toLowerCase().replaceAll("\\.", "")
					.replaceAll("\\'", "").split(" ");
			StringBuilder nameBuilder = new StringBuilder(100);
			for (String name : nameSet) {
				nameBuilder.append(name + "-");
			}
			String base = nameBuilder.toString();
			base = base.substring(0, base.length() - 1);
			return base;
		} else {
			String teamMascot = playerName.split(" D/ST")[0];
			String teamCity = teamName.split(teamMascot)[0];
			String[] nameSet = teamCity.toLowerCase().replaceAll("\\.", "")
					.split(" ");
			StringBuilder nameBuilder = new StringBuilder(100);
			for (String name : nameSet) {
				nameBuilder.append(name + "-");
			}
			nameBuilder.append("defense");
			return nameBuilder.toString();
		}
	}
}
