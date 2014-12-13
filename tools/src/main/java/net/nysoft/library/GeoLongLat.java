package net.nysoft.library;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;


public class GeoLongLat {

    public static void main(String argv[]) {

        try {
            URL url = new URL(
                    "http://maps.googleapis.com/maps/api/geocode/json?address="
                            + URLEncoder.encode("4186 Magellan Court, Dublin, CA 94568") + "&sensor=true");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output = "", full = "";
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                full += output;
            }

//            JSONObject json;
//            try {
//
//                String lat, lon;
//                json = new JSONObject(full);
//                JSONObject geoMetryObject = new JSONObject();
//                JSONObject locations = new JSONObject();
//                JSONArray jarr = json.getJSONArray("results");
//                int i;
//                for (i = 0; i < jarr.length(); i++) {
//                    json = jarr.getJSONObject(i);
//                    geoMetryObject = json.getJSONObject("geometry");
//                    locations = geoMetryObject.getJSONObject("location");
//                    lat = locations.getString("lat");
//                    lon = locations.getString("lng");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            conn.disconnect();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
