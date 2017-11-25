package net.iamblogger.wordlookup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import android.util.Log;

import java.util.List;

/**
 * Wordnik.java replaces Knicker and provides a simple access to Wordnik APIs with JSON response.
 */

public class Wordnik {

    private RequestQueue queue = Volley.newRequestQueue(MainActivity.context);
    private String apiKey;
    private String tokenStatusURL = "http://api.wordnik.com/v4/account.json/apiTokenStatus?api_key=";

    public class WordnikDefintionResult {
        boolean success;
        List<String> definitions;
    }

    public Wordnik(String key) {
        this.apiKey = key;
        this.tokenStatusURL = this.tokenStatusURL + this.apiKey;
    }

    protected void getDefinition(String word) {

        String requestURL = "http://api.wordnik.com/v4/word.json/"
                        + word
                        + "/definitions?"
                        + "limit=200"
                        + "&includeRelated=true"
                        + "&sourceDictionaries=all"
                        + "&useCanonical=true"
                        + "&includeTags=false"
                        + "&api_key=" + this.apiKey;

        addJsonRequest(requestURL);
    }

    private void addJsonRequest(String url) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("WORDNIK", "Response: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WORDNIK", "Error: ");

                    }
                }
        );

        this.queue.add(jsObjRequest);
    }

    protected void apiTokenStatus() {
        addJsonRequest(this.tokenStatusURL);
    }
}
