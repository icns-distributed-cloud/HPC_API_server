package com.icnslab.assist;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by alicek106 on 2017-08-04.
 */
public class JSONAssistant {
    public static String getString(JSONObject job, String arg){
        return (String)job.get(arg);
    }

    public static JSONObject parseJSON(String str){
        try {
            Object obj = JSONValue.parseWithException(str);
            JSONObject jobj = (JSONObject) obj;
            return jobj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
