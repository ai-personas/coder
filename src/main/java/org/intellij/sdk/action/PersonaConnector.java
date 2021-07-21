package org.intellij.sdk.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.intellij.sdk.settings.AppSettingsState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PersonaConnector {

    public String getGeneratedText(final String input) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        AppSettingsState appSettingsState = AppSettingsState.getInstance();
        String requestUrl = Objects.requireNonNull(appSettingsState.getState()).personaConnectorUrl;
        HttpPost httpPost = new HttpPost(requestUrl);

        String requestJson = new Gson().toJson(new PersonaInput(input));
        StringEntity entity = new StringEntity(requestJson);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);

        try {
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = response.getEntity();
                String responseJson = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                JsonArray jsonArray = new Gson().fromJson(responseJson, JsonArray.class);
                JsonObject jsonObject = (JsonObject) jsonArray.get(0);
                return jsonObject.get("generated_text").getAsString();
            } else {
                throw new IOException(response.getStatusLine().getReasonPhrase());
            }
        } finally {
            client.close();
        }
    }

    static class PersonaInput {

        protected String prompt;

        public PersonaInput(String prompt) {
            this.prompt=prompt;
        }
    }
}
