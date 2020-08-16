package com.devingotaswitch.utils

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object JsoupUtils {
    @JvmStatic
    @Throws(IOException::class)
    fun getDocument(url: String?): Document {
        return Jsoup.connect(url)
                .timeout(0)
                .get()
    }

    @Throws(IOException::class)
    private fun getDocumentWithUA(url: String): Document {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .followRedirects(true)
                .timeout(0)
                .get()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun parseURLWithoutUAOrTls(url: String, params: String): List<String> {
        val doc = getConnection(url)
                .timeout(0)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .get()
        return getElemsFromDoc(doc, params)
    }

    private fun getConnection(url: String): Connection {
        return Jsoup.connect(url)
                .timeout(0)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .sslSocketFactory(socketFactory())
    }

    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })
        return try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            sslContext.socketFactory
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to create a SSL socket factory", e)
        } catch (e: KeyManagementException) {
            throw RuntimeException("Failed to create a SSL socket factory", e)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun parseURLWithUA(url: String, params: String): List<String> {
        val doc = getDocumentWithUA(url)
        return getElemsFromDoc(doc, params)
    }

    @JvmStatic
    fun getElemsFromDoc(doc: Document, params: String): List<String> {
        val elems: MutableList<String> = ArrayList()
        val links = doc.select(params)
        for (element in links) {
            elems.add(element.text())
        }
        return elems
    }
}