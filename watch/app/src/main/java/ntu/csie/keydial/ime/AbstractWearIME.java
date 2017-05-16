package ntu.csie.keydial.ime;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import ntu.csie.keydial.AbstractKeyboardLayout.LetterCase;
import ntu.csie.keydial.AbstractKeyboardLayout.Mode;
import ntu.csie.keydial.AbstractPredictiveKeyboardLayout;
import ntu.csie.keydial.AutoComplete;
import ntu.csie.keydial.HapticFeedback;


public abstract class AbstractWearIME extends InputMethodService {


    private static final String TAG = "AbstractWearIME";


    protected AbstractPredictiveKeyboardLayout keyboard;


    @Override
    public View onCreateInputView() {
        Log.d(TAG, "onCreateInputView");

        keyboard = createKeyboardLayout(getApplicationContext());
        keyboard.setAutoComplete(new AutoComplete(getApplicationContext()));
        keyboard.setHapticFeedback(new HapticFeedback(this));
        keyboard.addSubmitListener(this::submit);

        return keyboard;
    }


    protected abstract AbstractPredictiveKeyboardLayout createKeyboardLayout(Context context);


    @Override
    public View onCreateExtractTextView() {
        return null;
    }


    @Override
    public View onCreateCandidatesView() {
        return null;
    }


    @Override
    public boolean onEvaluateFullscreenMode() {
        return true;
    }


    @Override
    public void setInputView(View view) {
        // force absolute full screen mode
        getWindow().setContentView(onCreateInputView());
    }


    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        Log.d(TAG, "onStartInputView");

        InputConnection ic = getCurrentInputConnection();
        String s = ic.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
        ic.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);

        Log.d(TAG, "BUFFER: " + s);
        keyboard.setText(s, Mode.LETTERS, LetterCase.UPPER);
    }


    public void submit(String s) {
        Log.d(TAG, "SUBMIT: " + s);

        // clear input
        InputConnection ic = getCurrentInputConnection();
        ic.commitText(s, s.length());

        sendDefaultEditorAction(true);
    }


}
