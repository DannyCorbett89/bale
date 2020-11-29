package com.dc.bale.component;

import com.dc.bale.exception.HttpException;
import okhttp3.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class HttpClient {
    private static final String EMPTY = "";

    private CloseableHttpClient client;
    private final JsonMapper jsonMapper;

    public HttpClient(JsonMapper jsonMapper) {
        SSLConnectionSocketFactory factory;
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            factory = new SSLConnectionSocketFactory(context);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            factory = null;
        }

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(200);
        manager.setDefaultMaxPerRoute(20);

        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(10000)
                        .build())
                .setSSLSocketFactory(factory)
                .setConnectionManager(manager)
                .build();
        this.jsonMapper = jsonMapper;
    }

    public String get(String url) {
        return get(url, null);
    }

    public String get(String url, String authorization) {
        HttpGet request = new HttpGet(url);

        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }

        return execute(request);
    }

    public void put(String url, ContentType contentType, String authorization, String content) {
        HttpPut put = new HttpPut(url);
        put.addHeader("Content-type", contentType.getMimeType());
        put.addHeader("Authorization", authorization);
        put.setEntity(new StringEntity(content, Charset.defaultCharset()));
        execute(put);
    }

    public <T> T multipart(String url, String username, String password, String formData, Class<T> returnType) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=---011000010111000001101001");
        RequestBody requestBody = RequestBody.create("-----011000010111000001101001\r\nContent-Disposition: form-data; " + formData + "\r\n-----011000010111000001101001--\r\n", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Authorization", getBasicAuth(username, password))
                .addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return jsonMapper.toObject(responseBody.string(), returnType);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    private String getBasicAuth(String username, String password) {
        String rawAuth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(rawAuth.getBytes());
    }

    private String execute(HttpRequestBase request) {
        try {
            CloseableHttpResponse response = client.execute(request);
            String value = EntityUtils.toString(response.getEntity());
            return value != null ? value : EMPTY;
        } catch (IOException e) {
            e.printStackTrace();
            return EMPTY;
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
