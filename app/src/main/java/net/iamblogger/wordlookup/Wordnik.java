package net.iamblogger.wordlookup;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Wordnik.java replaces Knicker and provides a simple access to Wordnik APIs with JSON response.
 */

class Wordnik {

    private String apiKey;
    private String tokenStatusURL = "http://api.wordnik.com/v4/account.json/apiTokenStatus?api_key=";

    class DefinitionResult {

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

    Wordnik(String key) {
        this.apiKey = key;
        this.tokenStatusURL = this.tokenStatusURL + this.apiKey;
    }

    ArrayList<DefinitionResult> getDefinition(String word, int limit) {

        String requestURL = "http://api.wordnik.com/v4/word.json/"
                        + Uri.encode(word)
                        + "/definitions?"
                        + "&includeRelated=true"
                        + "&sourceDictionaries=all"
                        + "&useCanonical=false"
                        + "&includeTags=false"
                        + "&api_key=" + this.apiKey;

        String line, json = "";
        JSONArray jsonArray;
        ArrayList<DefinitionResult> definitions = new ArrayList<DefinitionResult>();

        try {
            URL urls = new URL(requestURL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"));

            while ((line = reader.readLine()) != null) {
                json += line;
            }

            jsonArray = new JSONArray(json);

            for (int i = 0; i < limit; i++) {

                JSONObject definition = jsonArray.getJSONObject(i);

                DefinitionResult definitionResult = new DefinitionResult();

                definitionResult.setSourceDictionary(definition.getString("sourceDictionary"));
                definitionResult.setWord(definition.getString("word"));
                definitionResult.setPartOfSpeech(definition.getString("partOfSpeech"));
                definitionResult.setAttributionText(definition.getString("attributionText"));
                definitionResult.setText(definition.getString("text"));
                definitionResult.setScore(definition.getInt("score"));

                definitions.add(definitionResult);
            }

        } catch (Exception e) {
            Log.e("WORDNIK", "Error getting definitions from Wordnik.");
        }

        return definitions;
    }

    boolean apiTokenStatus() {

        boolean isValid = false;

        String line, json = "";
        JSONObject jsonObject;

        try {
            URL urls = new URL(tokenStatusURL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"));

            while ((line = reader.readLine()) != null) {
                json += line;
            }

            jsonObject = new JSONObject(json);

            if (jsonObject.getString("valid").equals("true")) {
                isValid = true;
            }

        } catch (Exception e) {
            Log.e("WORDNIK", "Error getting the API status from Wordnik.");
        }

        return isValid;
    }
}
