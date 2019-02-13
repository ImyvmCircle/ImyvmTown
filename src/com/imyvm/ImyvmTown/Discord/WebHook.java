package com.imyvm.ImyvmTown.Discord;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class WebHook {

    public void sendMessage(String webhook_url, String content) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response;
        HttpPost request = new HttpPost(webhook_url);
        request.addHeader("Content-Type", "application/json");
        String jsonMessage = "{\"content\": \"" + content + "\"}";
        try {
            StringEntity params = new StringEntity(jsonMessage, "UTF-8");
            request.setEntity(params);
            response = httpClient.execute(request);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        if (!(response.getStatusLine().getStatusCode() == 204)) {
            System.out.println(response.getStatusLine());
        }
    }
}
