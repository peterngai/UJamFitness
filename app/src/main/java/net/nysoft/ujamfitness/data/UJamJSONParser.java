package net.nysoft.ujamfitness.data;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 * TODO: Write Javadoc for UJamJSONParser.
 *
 * @author pngai
 */
public class UJamJSONParser extends AsyncTask<Object, Void, Object> {

    private final static String JSON_SITE = "http://www.outrageousthoughts.com/server_ujamgymclasses.json";

    protected UJamGyms _gyms;

    public UJamGyms getGyms() {
        return _gyms;
    }

    public interface OnTaskCompleted{
        void onTaskCompleted();
    }

    private OnTaskCompleted listener;

    public UJamJSONParser(OnTaskCompleted listener){
        this.listener = listener;
    }

    protected String doInBackground(Object... params) {

        HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object
        HttpGet httpget = new HttpGet(JSON_SITE);

        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();

                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                JsonFactory jsonFactory = new JsonFactory();
                JsonParser jp = jsonFactory.createJsonParser(instream);
                _gyms = objectMapper.readValue(jp, UJamGyms.class);
                Log.d("UJamClassFetcher", "completed loading json.");
            }
        } catch (Exception e) {
            e.getMessage() ;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {

        Log.d("UJamClassFetcher", "in onPostExecute");

        if (listener != null)
            listener.onTaskCompleted();
    }
}

