package net.iamblogger.wordlookup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LookupCore extends AsyncTask<String, Integer, String>{
    private String result; 
    public String finalword;
    public String word = "";
    
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
        System.setProperty(
                "WORDNIK_API_KEY",
                "" + MainActivity.context.getString(R.string.wordnik_key)
        );


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
        finalword = wordarray[0];

        if (!Init()) {
            result = word +": Error, check your network connection.";
            return;
        }

        // check the status of the API key
        TokenStatus status = AccountApi.apiTokenStatus();
        if (status.isValid()) {
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

        String finalword_encoded = "";

        List<Definition> def = new ArrayList<Definition>();

        sourceDictionary.add(Knicker.SourceDictionary.all);

        while (!definitionFetchComplete && retryCount <= 30) {

            finalword = CleanInput(finalword);

            def = WordApi.definitions(
                    finalword,
                    limit,
                    null,
                    true,
                    sourceDictionary,
                    true,
                    false
            );

            Log.i("SWIFTDICT","Found " + def.size() + " definitions.");

            for (Definition d : def) {
                result = d.getWord() + ": \n";
                word = d.getWord();
            }

            String onlyDefinitionText = "";

            if (def.size() == 1) {
                onlyDefinitionText = def.get(0).getText();

                onlyDefinitionText = onlyDefinitionText.toLowerCase();

                if (onlyDefinitionText.matches("^.+\\s+form\\s+of\\s+([a-z|0-9]+).*$")) {
                    finalword = onlyDefinitionText.replaceAll(".+\\s+form\\s+of\\s+", "");
                    retryCount++;
                } else {
                    definitionFetchComplete = true;
                }

            } else if (def.size() == 0) {
                PorterStemmer stemmer = new PorterStemmer();

                stemmedWord = stemmer.stemWord(finalword);

                if (stemmedWord.equals(finalword)) {
                    definitionFetchComplete = true;
                } else {
                    finalword = stemmedWord;
                    retryCount++;
                }
            } else {
                definitionFetchComplete = true;
            }
        }

        int i = 1;

        for (Definition d : def) {
            result += i + ") " + d.getPartOfSpeech() + ": " + d.getText() + "\n";
            Log.d("WORDLOOKUP",result);
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
            result = finalword +": No definition found.";
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
