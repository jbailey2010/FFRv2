package com.devingotaswitch.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class JsoupUtils {

    public static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .timeout(0)
                .get();
    }

    private static Document getDocumentWithUA(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .followRedirects(true)
                .timeout(0)
                .get();
    }

    public static List<String> parseURLWithoutUAOrTls(String url, String params) throws IOException {
        Document doc = getConnection(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .get();
        return getElemsFromDoc(doc, params);
    }

    private static Connection getConnection(String url){
        return Jsoup.connect(url)
                .timeout(0)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .sslSocketFactory(socketFactory());
    }

    private static SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory result = sslContext.getSocketFactory();

            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }

    public static List<String> parseURLWithUA(String url, String params)
            throws IOException {
        Document doc = getDocumentWithUA(url);
        return getElemsFromDoc(doc, params);
    }

    public static List<String> getElemsFromDoc(Document doc, String params) {
        List<String> elems = new ArrayList<>();
        Elements links = doc.select(params);
        for (Element element : links) {
            elems.add(element.text());
        }
        return elems;
    }
}
