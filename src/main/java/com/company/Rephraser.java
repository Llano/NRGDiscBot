package com.company;


import net.dv8tion.jda.internal.utils.EncodingUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Rephraser {

    public static String Rephrase(String str) {
        String urlString = "https://quillbot.com/api/singleParaphrase?userID=N/A&text=" + str.replace(" ", "%20") + "&strength=0&autoflip=true&wikify=true&fthresh=9";
        URL url = null;
        try {
            url = new URL(urlString);

            URLConnection conn = url.openConnection();
            conn.setRequestProperty("cookie", Main.config.getString("quillbot.cookies"));


            InputStream is = conn.getInputStream();
            String para = IOUtils.toString(is, "utf-8");
            JSONArray o = new JSONArray(para);
            return o.getJSONObject(0).getJSONArray("paras_1").getJSONObject(2).getString("alt");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
