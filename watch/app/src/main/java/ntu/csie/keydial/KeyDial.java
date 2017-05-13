package ntu.csie.keydial;


import android.content.Context;
import android.support.wearable.view.CurvedChildLayoutManager;
import android.widget.Button;

public class KeyDial extends AbstractPredictiveKeyboardLayout {


    public static final int ALPHA = R.layout.keydial_alpha;
    public static final int ALPHA_ZIGZAG = R.layout.keydial_alpha_zigzag;
    public static final int QWERTY_ZIGZAG = R.layout.keydial_qwerty_zigzag;


    public KeyDial(Context context, int layout) {
        super(context, layout);

        // use curved vertical suggestion recycler
        this.suggestionView.setLayoutManager(new CurvedChildLayoutManager(context));
    }


    @Override
    protected int[] getButtonGroups() {
        return new int[]{R.id.letters, R.id.numbers, R.id.controls};
    }


    @Override
    protected int getEditorLayout() {
        return R.id.text_editor;
    }


    @Override
    protected int getSuggestionRecyclerLayout() {
        return R.id.suggestion_recycler;
    }


    @Override
    protected int getSuggestionItemLayout() {
        return R.layout.item_suggestion_vertical;
    }

    @Override
    protected int getModeLayout(Mode mode) {
        switch (mode) {
            case LETTERS:
                return R.id.letters;
            case NUMBERS_AND_PUNCTUATION:
                return R.id.numbers;
        }
        return -1;
    }


    @Override
    protected void highlight(String key, Button button, boolean enabled) {
        super.highlight(key, button, enabled);

        // use background highlighting as well as text highlighting
        // backgroundHighlight(button, enabled);
    }


    @Override
    public void setLetterCase(LetterCase letterCase) {
        super.setLetterCase(letterCase);

        // toggle option button
        backgroundHighlight(keys.get(Symbols.OPTION), letterCase == LetterCase.UPPER);
    }


    @Override
    public void setMode(Mode mode) {
        super.setMode(mode);

        // toggle mode button
        backgroundHighlight(keys.get(Symbols.KEYBOARD), mode == Mode.LETTERS);
    }


    protected void backgroundHighlight(Button button, boolean enabled) {
        button.setBackgroundResource(enabled ? R.drawable.keydial_button_alt_bg : R.drawable.keydial_button_bg);
    }


}
