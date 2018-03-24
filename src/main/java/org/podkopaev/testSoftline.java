package org.podkopaev;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class testSoftline {
    private static String uri = "http://93.174.135.221:8080/sd/services/rest/";
    private static String access = "?accessKey=165cdad7-6a00-4c47-a41d-4c0122422167";
    private static HttpGet request;
    private static HttpResponse response;
    private static BufferedReader reader;
    private static CloseableHttpClient client;
    private static Long key1;
    private static String answer;
    private static String next;
    private static String uuid;
    private static int counter;

    public static void main(String[] args) {
        client = HttpClientBuilder.create().build();
        //Создание объекта
        executeRequest("create/test$entry", "&parent=employee$2080708");
        //Поиск созданного объекта по родительскому
        readJSON(executeRequest("find/test$entry", "&parent=employee$2080708"));
        //Простановка статуса Closed
        executeRequest("edit/" + uuid, "&state=closed");
        readJSON(executeRequest("get/" + uuid, ""));
        //Цикл поиска ответа
        while (answer == null) {
            executeRequest("edit/" + next, "&response=" + key1);
            executeRequest("edit/" + next, "&state=closed");
            readJSON(executeRequest("get/" + next, ""));
            System.out.println(counter++);
        }
        System.out.println(answer);
        try {
            client.close();
        } catch (IOException ex) {
            System.out.println("Error Close client");
            ex.printStackTrace();
        }
    }

    //Считывание и разбор ответа от сервера
    private static void readJSON(String stringJSON) {
        JSONParser parser = new JSONParser();
        JSONArray array;
        JSONObject obj = null;
        try {
            if (parser.parse(stringJSON) instanceof JSONArray) {
                array = (JSONArray) parser.parse(stringJSON);
                /*System.out.println(array);
                System.out.println("--------");*/
                obj = (JSONObject) array.get(0);
            } else obj = (JSONObject) parser.parse(stringJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /*System.out.println(obj.toJSONString());*/
        key1 = (Long) obj.get("key1");
        answer = (String) obj.get("answer");
        if (obj.get("next") != null) {
            JSONObject objNext = (JSONObject) obj.get("next");
            next = (String) objNext.get("UUID");
        } else {
            next = (String) obj.get("next");
        }
        uuid = (String) obj.get("UUID");
/*        System.out.println("key = " + key1);
        System.out.println("answer = " + answer);
        System.out.println("next = " + next);
        System.out.println("uuid = " + uuid);
        System.out.println("----");*/
    }


    private static String executeRequest(String command, String attributes) {
        request = new HttpGet(uri + command + access + attributes);
        String responseString = "";
        try {
            response = client.execute(request);
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                responseString = line;
            }
        } catch (IOException ex) {
            System.out.println("Request exception");
            ex.printStackTrace();
        }
        return responseString;
    }
}
