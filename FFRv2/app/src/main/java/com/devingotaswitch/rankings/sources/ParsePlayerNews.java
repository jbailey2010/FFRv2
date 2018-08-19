package com.devingotaswitch.rankings.sources;

import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ParsePlayerNews {

    private static final String TAG = "ParsePlayerNews";


    public static void startNews(String playerName, String playerTeam, PlayerInfo activity) {
        String baseUrl = "http://www.fantasypros.com/nfl/news/"
                + playerNameUrl(playerName, playerTeam) + ".php";
        String notesUrl = "http://www.fantasypros.com/nfl/notes/"
                + playerNameUrl(playerName, playerTeam) + ".php";
        NewsParser objParse = new NewsParser(activity, baseUrl, notesUrl);
        objParse.execute();
    }

    static class NewsParser extends
            AsyncTask<Object, String, List<PlayerNews>> {
        final PlayerInfo act;
        String urlNews;
        final String urlNotes;

        NewsParser(PlayerInfo activity, String url, String altUrl) {
            act = activity;
            urlNews = url;
            urlNotes = altUrl;
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
                Document doc = JsoupUtils.getDocument(urlNotes);
                Elements noteElems = doc.select("div.body-row div.content");
                try {
                    for (Element element : noteElems) {
                        // First, get the 'notes'
                        String title = element.child(0).text();
                        String date = element.parent().parent().child(1).child(1).text();
                        String author = element.parent().parent().child(1).child(0).text();
                        PlayerNews news = new PlayerNews();
                        news.setNews(title);
                        news.setImpact(author + Constants.LINE_BREAK + date);

                        newsList.add(news);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get player notes from " + urlNews, e);
                }
                doc = JsoupUtils.getDocument(urlNews);
                noteElems = doc.select("div.body-row div.content");
                for (Element element : noteElems) {
                    // Then get the 'news'
                    String title = element.child(1).text();
                    String body = element.child(3).text();
                    String date = element.parent().parent().child(1).child(1).text();
                    String author = element.parent().parent().child(1).child(0).text();
                    PlayerNews news = new PlayerNews();
                    news.setNews(title);
                    news.setImpact(body + Constants.LINE_BREAK + Constants.LINE_BREAK + author + Constants.LINE_BREAK + date);

                    newsList.add(news);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get news", e);
                return newsList;
            }
            return newsList;
        }

    }

	public static String playerNameUrl(String playerName, String teamName) {

		if (!playerName.contains(Constants.DST)) {
			String[] nameSet = playerName.toLowerCase().replaceAll("\\.", "")
					.replaceAll("\'", "").split(" ");
			StringBuilder nameBuilder = new StringBuilder(100);
			for (String name : nameSet) {
				nameBuilder.append(name)
                        .append("-");
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
				nameBuilder.append(name)
                        .append("-");
			}
			nameBuilder.append("defense");
			return nameBuilder.toString();
		}
	}
}
