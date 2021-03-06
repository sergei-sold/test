package com.siska.blog4prog.translator;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeeplTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeeplTranslator.class);
    private static final String TRANSLATOR_ENDPOINT_URL = "https://www2.deepl.com/jsonrpc";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";
    String JSON = "{\r\n" + "    \"jsonrpc\": \"2.0\",\r\n" + "    \"method\": \"LMT_handle_jobs\",\r\n"
            + "    \"params\": {\r\n" + "        \"jobs\": [\r\n" + "            {\r\n"
            + "                \"kind\": \"default\",\r\n" + "                \"raw_en_sentence\": \"%s\",\r\n"
            + "                \"raw_en_context_before\": [],\r\n" + "                \"raw_en_context_after\": [\r\n"
            + "                    \r\n" + "                ]\r\n" + "            }\r\n" + "        ],\r\n"
            + "        \"lang\": {\r\n" + "            \"user_preferred_langs\": [\r\n" + "                \"RU\",\r\n"
            + "                \"EN\"\r\n" + "            ],\r\n" + "            \"source_lang_computed\": \"EN\",\r\n"
            + "            \"target_lang\": \"RU\"\r\n" + "        },\r\n" + "        \"priority\": 1,\r\n"
            + "        \"timestamp\": %s\r\n" + "    },\r\n" + "    \"id\": %s\r\n" + "}";
    private String proxyString;
    private int responseCode = 0;

    public String translate(String text) throws UnsupportedEncodingException {
        String result = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            HttpURLConnection connection = null;
            if (proxyString != null && proxyString != "") {
                String[] split = proxyString.split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
                connection = (HttpURLConnection) new URL(TRANSLATOR_ENDPOINT_URL).openConnection(proxy);
            } else {
                connection = (HttpURLConnection) new URL(TRANSLATOR_ENDPOINT_URL).openConnection();
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("user-Agent", USER_AGENT);
            connection.setRequestProperty("Content-type", "text/plain");
            connection.setRequestProperty("accept", "text/xml");
            connection.setRequestMethod("POST");

            String timestamp = "" + (new Date()).getTime();
            String id = "7580090";
            String json = String.format(JSON, text, timestamp, id);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.getBytes());
            outputStream.flush();
            outputStream.close();

            responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                DeeplResponse value = mapper.readValue(IOUtils.toString(connection.getInputStream(), "UTF-8"),
                        DeeplResponse.class);
                result = value.result.translations.get(0).beams.get(0).processedSentence;
            }
        } catch (Exception e) {
            LOGGER.error("Error during translation. proxy=" + proxyString, e);
        }
        return result;
    }

    public void setPoxy(String proxy) {
        this.proxyString = proxy;
    }

    public int getResponseCode() {
        return responseCode;
    }

}
