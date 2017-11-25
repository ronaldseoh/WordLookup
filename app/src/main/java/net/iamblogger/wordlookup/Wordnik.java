package net.iamblogger.wordlookup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * Wordnik.java replaces Knicker and provides a simple access to Wordnik APIs with JSON response.
 */

class Wordnik {

    private RequestQueue queue = Volley.newRequestQueue(MainActivity.context);
    private String apiKey;
    private String tokenStatusURL = "http://api.wordnik.com/v4/account.json/apiTokenStatus?api_key=";

    class WordnikDefinitionResult {

        private boolean success;

        private String text;

        private String word;

        private String partOfSpeech;

        private String sourceDictionary;

        private int score;

        private String attributionText;

        String getText() {
            return text;
        }

        String getWord() {
            return word;
        }

        String getPartOfSpeech() {
            return partOfSpeech;
        }

        String getSourceDictionary() {
            return sourceDictionary;
        }

        int getScore() {
            return score;
        }

        String getAttributionText() {
            return attributionText;
        }

        void setText(String text) {
            this.text = text;
        }

        void setWord(String word) {
            this.word = word;
        }

        void setPartOfSpeech(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        void setSourceDictionary(String sourceDictionary) {
            this.sourceDictionary = sourceDictionary;
        }

        void setScore(int score) {
            this.score = score;
        }

        void setAttributionText(String attributionText) {
            this.attributionText = attributionText;
        }
    }

    public Wordnik(String key) {
        this.apiKey = key;
        this.tokenStatusURL = this.tokenStatusURL + this.apiKey;
    }

    protected WordnikDefinitionResult getDefinition(String word) {

        String requestURL = "http://api.wordnik.com/v4/word.json/"
                        + Uri.encode(word)
                        + "/definitions?"
                        + "&includeRelated=true"
                        + "&sourceDictionaries=all"
                        + "&useCanonical=true"
                        + "&includeTags=false"
                        + "&api_key=" + this.apiKey;

        final WordnikDefinitionResult definitionResult = new WordnikDefinitionResult();

        JsonArrayRequest jsArrayRequest = new JsonArrayRequest (
                Request.Method.GET,
                requestURL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("WORDNIK", "Wordnik Response Success!");
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject definition = response.getJSONObject(i);
                                definitionResult.setSourceDictionary(definition.getString("sourceDictionary"));
                                definitionResult.setWord(definition.getString("word"));
                                definitionResult.setPartOfSpeech(definition.getString("partOfSpeech"));
                                definitionResult.setAttributionText(definition.getString("attributionText"));
                                definitionResult.setText(definition.getString("text"));
                                definitionResult.setScore(definition.getInt("score"));
                            } catch (JSONException e) {
                                Log.e("WORDNIK", "Error parsing response");
                                break;
                            }


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WORDNIK", "Wordnik Response Error!");

                    }
                }
        );

        this.queue.add(jsArrayRequest);

        return definitionResult;
    }

    protected void apiTokenStatus() {
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest (
                Request.Method.GET,
                this.tokenStatusURL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("WORDNIK", "Wordnik Response Success!");
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject definition = response.getJSONObject(i);
                                definitionResult.setSourceDictionary(definition.getString("sourceDictionary"));
                                definitionResult.setWord(definition.getString("word"));
                                definitionResult.setPartOfSpeech(definition.getString("partOfSpeech"));
                                definitionResult.setAttributionText(definition.getString("attributionText"));
                                definitionResult.setText(definition.getString("text"));
                                definitionResult.setScore(definition.getInt("score"));
                            } catch (JSONException e) {
                                Log.e("WORDNIK", "Error parsing response");
                                break;
                            }


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WORDNIK", "Wordnik Response Error!");

                    }
                }
        );

        this.queue.add(jsArrayRequest);
    }
}
