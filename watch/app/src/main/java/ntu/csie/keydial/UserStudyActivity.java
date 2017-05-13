package ntu.csie.keydial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class UserStudyActivity extends WearableActivity {

    public static final String EXTRA_KEYBOARD_LAYOUT = "KeyboardLayout";
    public static final String EXTRA_PHRASE_COUNT = "PhraseCount";


    private AbstractPredictiveKeyboardLayout keyboard;
    private Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Intent that started this activity and extract the select keyboard
        Intent intent = getIntent();
        KeyboardLayout layout = KeyboardLayout.valueOf(intent.getStringExtra(EXTRA_KEYBOARD_LAYOUT));
        int phraseCount = intent.getIntExtra(EXTRA_PHRASE_COUNT, -1);

        Log.d("UserStudyActivity", "Create Keyboard: " + layout);
        recorder = new Recorder(getApplicationContext(), RECORDER_NODE, RECORDER_SESSION, Build.MODEL);

        keyboard = layout.createView(getApplicationContext());
        keyboard.setRecorder(recorder);
        keyboard.setAutoComplete(phraseCount == OOV_SESSION_PHRASE_COUNT ? null : getSharedAutoCompleteInstance(getApplicationContext()));
        keyboard.setHapticFeedback(new HapticFeedback(this));

        // display canned replies instead of nothing
        // keyboard.setSuggestions(asList(getResources().getStringArray(R.array.commands)));

        // keep track of the user study session
        UserStudyObserver observer = new UserStudyObserver(this, layout, phraseCount, recorder);
        keyboard.addSubmitListener(observer);

        setContentView(keyboard);

        // make sure that screen doesn't turn off during user study
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // forward watch hardware button events to soft keyboard
        return keyboard.onKeyDown(keyCode, event);
    }


    // shared instance to avoid potential process memory limits
    private static AutoComplete sharedAutoCompleteInstance;

    public static AutoComplete getSharedAutoCompleteInstance(Context context) {
        if (sharedAutoCompleteInstance == null) {
            sharedAutoCompleteInstance = new AutoComplete(context);
        }

        return sharedAutoCompleteInstance;
    }


    // public static final String RECORDER_NODE = "http://10.0.1.2:22148/record";
    public static final String RECORDER_NODE = "http://oasis1.csie.ntu.edu.tw:22148/record";
    public static final String RECORDER_SESSION = String.format("%08X", System.currentTimeMillis());

    public static final KeyboardLayout[] MAIN_SESSION_KEYBOARD_LAYOUTS = {KeyboardLayout.KeyDialQwertyZigZag, KeyboardLayout.SwipeKeyQwerty, KeyboardLayout.StandardQwerty};

    public static final int MAIN_SESSION_PHRASE_COUNT = 20;
    public static final int OOV_SESSION_PHRASE_COUNT = 5;


    public static class UserStudyObserver implements Consumer<String> {


        public static final String CODE_START = "START";
        public static final String CODE_END = "END";
        public static final String CODE_EXIT = "EXIT";


        private AtomicInteger phraseIndex = new AtomicInteger(-1);
        private int phraseCount = 5;


        private Activity activity;
        private Recorder recorder;


        public UserStudyObserver(Activity activity, KeyboardLayout layout, int phraseCount, Recorder recorder) {
            this.activity = activity;
            this.phraseCount = phraseCount;
            this.recorder = recorder;

            // add phrase index to records
            this.recorder.setProgress(layout, phraseIndex);
        }

        @Override
        public void accept(String s) {
            if (phraseIndex.get() < 0 && s.isEmpty()) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    activity.finish();
                }, 100);
                return;
            }


            if (s.equals(CODE_EXIT)) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    activity.finish();
                    System.exit(0);
                }, 100);
            }


            if (s.equals(CODE_START)) {
                phraseIndex.set(0); // phrase zero is character training
                recorder.setEnabled(true);
                recorder.record(Symbols.START_OF_TEXT, Symbols.START_OF_TEXT);

                // make sure that prediction index is in memory
                IntStream.rangeClosed('A', 'Z').mapToObj(c -> Character.toString((char) c)).forEach(k -> {
                    getSharedAutoCompleteInstance(activity.getApplicationContext()).getSuggestionsAsync(k, InputType.ENTER_LETTER, k, l -> Log.d("AutoComplete", l.toString()));
                });

                return;
            }

            if (phraseIndex.get() >= 0) {
                // phrase complete
                if (phraseIndex.incrementAndGet() > phraseCount || s.equals(CODE_END)) {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        recorder.record(Symbols.END_OF_TEXT, Symbols.END_OF_TEXT);
                        recorder.setEnabled(false);
                        activity.finish();
                    }, 100);
                }
            }
        }


    }

}

