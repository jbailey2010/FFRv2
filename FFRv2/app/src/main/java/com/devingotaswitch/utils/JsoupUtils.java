package com.devingotaswitch.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsoupUtils {

    public static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    public static Document getDocumentWithUA(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .followRedirects(true)
                .timeout(0)
                .get();
    }

    public static List<String> handleLists(String url, String params)
            throws IOException {
        List<String> elems = new ArrayList<>();
        Document doc = getDocumentWithUA(url);
        Elements links = doc.select(params);
        for (Element element : links) {
            elems.add(element.text());
        }
        return elems;
    }

    public static List<String> handleListsMulti(Document doc, String params) throws IOException {
        List<String> elems = new ArrayList<>();
        Elements links = doc.select(params);
        for (Element element : links) {
            elems.add(element.text());
        }
        return elems;
    }
}
