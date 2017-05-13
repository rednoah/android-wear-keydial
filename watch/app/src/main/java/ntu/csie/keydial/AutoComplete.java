package ntu.csie.keydial;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class AutoComplete {


    private Context context;


    public AutoComplete(Context context) {
        this.context = context;
    }


    // cache first letter completions for performance reasons
    private final Map<String, List<String>> cache = new HashMap<String, List<String>>(26 * 26);

    public List<String> getSuggestions(String key, InputType type, String buffer) {
        if (buffer.length() < 3) {
            return cache.computeIfAbsent(buffer.toUpperCase(), k -> getSuggestions(buffer));
        }

        return getSuggestions(buffer);
    }


    private List<String> getSuggestions(String buffer) {
        return getAdaptxtSuggestions(buffer);
    }


    public void getSuggestionsAsync(String key, InputType type, String buffer, Consumer<List<String>> handler) {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {
                return getSuggestions(key, type, buffer);
            }

            @Override
            protected void onPostExecute(List<String> suggestions) {
                handler.accept(suggestions);
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    // adaptxt only works on armv7
    private AdaptxtCoreEngine adaptxtEngine;

    // java implementation works on all platforms
    private Prediction wordFrequencyMap;


    private AdaptxtCoreEngine getAdaptxtEngine() {
        if (adaptxtEngine == null) {
            adaptxtEngine = AdaptxtCoreEngine.getCoreEngineImpl();
            adaptxtEngine.prepareCoreFiles(context.getFilesDir().getAbsolutePath(), context.getAssets());
            adaptxtEngine.initializeCore();
            adaptxtEngine.setErrorCorrection(true, 20);
        }
        return adaptxtEngine;
    }


    private List<String> suggestions = new ArrayList<String>();

    private synchronized List<String> getAdaptxtSuggestions(String s) {
        // list exact matches in suggestions (i.e. work around Adaptxt behaviour)
        if (s.length() > 0 && !s.endsWith(" ")) {
            String w = s.substring(s.lastIndexOf(" ") + 1);
            List<String> matches = suggestions.stream().filter(e -> e.equalsIgnoreCase(w)).collect(toList());
            suggestions.clear();
            suggestions.addAll(matches);
        } else {
            suggestions.clear();
        }

        AdaptxtCoreEngine adaptxt = getAdaptxtEngine();
        List<AdaptxtCoreEngine.KPTSuggestion> kptSuggestions = adaptxt.insertTextAndGetSuggestions(s.toLowerCase());

        //Log.d("KPT", String.valueOf(kptSuggestions));
        if (kptSuggestions != null) {
            kptSuggestions.stream()
                    .map(AdaptxtCoreEngine.KPTSuggestion::getSuggestionString)
                    .filter(w -> !suggestions.contains(w))
                    .forEach(suggestions::add);
        }

        return new ArrayList<String>(suggestions);
    }


    private Prediction getWordFrequencyMap() {
        if (wordFrequencyMap == null) {
            File assetsFolder = prepareAssets(context, "prediction.dat", "prediction.dat.p", "prediction.dat.t");
            wordFrequencyMap = new Prediction(new File(assetsFolder, "prediction.dat"));
        }
        return wordFrequencyMap;
    }


    private List<String> getWordFrequencyMapSuggestions(String s) {
        return getWordFrequencyMap().completeSentence(s, 50);
    }


    private File prepareAssets(Context context, String... assets) {
        try {
            File assetsFolder = new File(context.getFilesDir(), "assets");
            if (!assetsFolder.exists() && !assetsFolder.mkdirs()) {
                throw new IllegalStateException("Failed to create folder: " + assetsFolder);
            }

            for (String a : assets) {
                Log.d("AutoComplete", "Prepare asset: " + a);
                FileUtils.copyInputStreamToFile(context.getAssets().open(a, AssetManager.ACCESS_STREAMING), new File(assetsFolder, a));
            }

            return assetsFolder;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
