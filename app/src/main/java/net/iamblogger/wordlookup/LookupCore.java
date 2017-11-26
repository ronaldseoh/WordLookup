package net.iamblogger.wordlookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

public class LookupCore extends AsyncTask<String, Integer, String>{
    private String result; 
    public String finalWord;
    public String word = "";

    // API key here
    // Instead of hard-coding the api key, I created another
    // resource xml file to store my api key.
    // If you want to compile this project yourself,
    // go to http://developer.wordnik.com, get your own key,
    // and store it as a string with name 'wordnik_key'
    // (You probably would want to store that string in a separate xml file
    // not tracked by git)
    // NOTE: R.string.<name> does not return the string value itself;
    // It just gives you the id
    private Wordnik wordnikController = new Wordnik(
            MainActivity.context.getString(R.string.wordnik_key)
    );
    
    private static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                MainActivity.context.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    private static boolean Init() {
        boolean networkState = isNetworkAvailable();

        if (!networkState) return false;

        return true;
    }

    /* Input Sanitization */
    private static String CleanInput(String input) throws Exception {

        //Lower case and clean up everything
        String cleanedInput = input.toLowerCase();

        //removes symbols from first and last character
        cleanedInput = cleanedInput.replaceAll("^[^\\p{L}\\p{N}]$*","");

        return cleanedInput;
    }
    
    public void GetDefinition(String[] wordarray) throws Exception {

        word = wordarray[0];
        finalWord = wordarray[0];

        if (!Init()) {
            result = word +": Error, check your network connection.";
            return;
        }

        // check the status of the API key
        Boolean apiOK = wordnikController.apiTokenStatus();

        if (apiOK) {
            Log.i("SWIFTDICT", "API key is valid.");
        } else {
            Log.e("SWIFTDICT", "API key is invalid.");
            result = "API key is invalid!";
        }

        // get a list of definitions for a word
        int limit = Integer.parseInt(MainActivity.NumOfDef);

        boolean definitionFetchComplete = false;

        int retryCount = 0;

        String stemmedWord = "";

        String finalWord_encoded = "";

        ArrayList<Wordnik.DefinitionResult> def = new ArrayList<Wordnik.DefinitionResult>();

        while (!definitionFetchComplete && retryCount <= 30) {

            finalWord = CleanInput(finalWord);

            def = wordnikController.getDefinition(finalWord, limit);

            Log.i("SWIFTDICT","Found " + def.size() + " definitions.");

            for (Wordnik.DefinitionResult d : def) {
                result = d.getWord() + ": \n";
                word = d.getWord();
            }

            String onlyDefinitionText = "";

            if (def.size() == 1) {
                onlyDefinitionText = def.get(0).getText();

                onlyDefinitionText = onlyDefinitionText.toLowerCase();

                if (onlyDefinitionText.matches("^.+\\s+form\\s+of\\s+([a-z|0-9]+).*$")) {
                    finalWord = onlyDefinitionText.replaceAll(".+\\s+form\\s+of\\s+", "");
                    retryCount++;
                } else {
                    definitionFetchComplete = true;
                }

            } else if (def.size() == 0) {
                PorterStemmer stemmer = new PorterStemmer();

                stemmedWord = stemmer.stemWord(finalWord);

                if (stemmedWord.equals(finalWord)) {
                    definitionFetchComplete = true;
                } else {
                    finalWord = stemmedWord;
                    retryCount++;
                }
            } else {
                definitionFetchComplete = true;
            }
        }

        int i = 1;

        for (Wordnik.DefinitionResult d : def) {
            result += i + ") " + d.getPartOfSpeech() + ": " + d.getText() + "\n";
            Log.d("WORDLOOKUP", result);
            i++;
        }
    }

    @Override
    protected String doInBackground(String... words) {
        try {
            GetDefinition(words);
        } catch (Exception e) {
            e.printStackTrace();
            //Looper -> workaround
            Looper.prepare();
            result = e.toString();
            Handler handler=new Handler();
            handler.post(runOnResult);
        }

        return null;
    }
    @Override
    protected void onPostExecute(String arg) {
        // async task finished
        Handler handler=new Handler();
        Log.v("SWIFTDICT", "Progress Finished.");

        if (result != null) {
            handler.post(runOnResult);
        } else {
            result = finalWord +": No definition found.";
            handler.post(runOnResult);
        }
    }

    private Runnable runOnResult = new Runnable() {
        public void run() {
            if (MainActivity.toggleTTS) {
                String[] ThingsToSay = {word,result};
                TTS mTTS = new TTS();
                mTTS.execute(ThingsToSay);
            }

            ShowToast.show(result);
        }
    };
}
