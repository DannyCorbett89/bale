package com.dc.bale.component;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

@Component
public class HttpClient {

    private CloseableHttpClient client;

    public HttpClient() {
        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(2000)
                        .setSocketTimeout(5000)
                        .build())
                .build();
    }

    public Optional<String> get(String url) {
        return get(url, null);
    }

    public Optional<String> get(String url, String authorization) {
        HttpGet request = new HttpGet(url);

        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }

        return execute(request);
    }

    public Optional<String> put(String url, ContentType contentType, String authorization, String content) {
        HttpPut put = new HttpPut(url);
        put.addHeader("Content-type", contentType.getMimeType());
        put.addHeader("Authorization", authorization);
        put.setEntity(new StringEntity(content, Charset.defaultCharset()));
        return execute(put);
    }

    private Optional<String> execute(HttpRequestBase request) {
        try {
            CloseableHttpResponse response = client.execute(request);
            return Optional.of(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
