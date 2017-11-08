package net.iamblogger.wordlookup;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import net.jeremybrooks.knicker.AccountApi;
import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.dto.Definition;
import net.jeremybrooks.knicker.dto.TokenStatus;

public class API extends AsyncTask<String, Integer, String>{
	private String result; 
	public String finalword;
	public String word = "";
	
	private static boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    	System.setProperty("WORDNIK_API_KEY", "" + MainActivity.context.getString(R.string.wordnik_key));
		return true;
	}
	
    public void GetDefinition(String[] wordarray) throws Exception {

    	//Lower case and clean up everything
		word = wordarray[0];
		String lcword = word.toLowerCase();
		String lettersword = lcword.replaceAll("^[^\\p{L}\\p{N}]$",""); //removes symbols from first and last character
		finalword = lettersword.replaceAll("[\\s]","%20"); //replaces spaces

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
		/* TODO:
		 * It's a mess.
		 * Make them optionable
		 * CLEAN UP
		 */
		int limit = Integer.parseInt(MainActivity.NumOfDef);

		PorterStemmer stemmer = new PorterStemmer();

		finalword = stemmer.stemWord(finalword);

		List<Definition> def = WordApi.definitions(finalword, limit,null, true, null, true, false);

		Log.i("SWIFTDICT","Found " + def.size() + " definitions.");

		for (Definition d : def) {
			result = d.getWord() + ": \n";
			word = d.getWord();
		}

		int i = 1;

		for (Definition d : def) {
			//		return((i++) + ") " + d.getPartOfSpeech() + ": " + d.getText());
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
			// TODO Auto-generated catch block
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
