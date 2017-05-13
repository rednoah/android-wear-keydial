package ntu.csie.keydial;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;


public abstract class AbstractPredictiveKeyboardLayout extends AbstractKeyboardLayout {

    protected WearableRecyclerView suggestionView;
    protected int suggestionLeadHighlightColor;

    protected AutoComplete autoComplete;


    public AbstractPredictiveKeyboardLayout(Context context, int layout) {
        super(context, layout);

        this.suggestionView = (WearableRecyclerView) findViewById(getSuggestionRecyclerLayout());
        this.suggestionView.setHasFixedSize(true);

        this.suggestionLeadHighlightColor = getResources().getColor(R.color.suggestion_lead_fg, getContext().getTheme());
    }


    protected abstract int getSuggestionRecyclerLayout();


    protected abstract int getSuggestionItemLayout();


    public void setAutoComplete(AutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }


    @Override
    public void keyPressed(String key, InputType type) {
        super.keyPressed(key, type);

        if (autoComplete != null && type != InputType.CONTROL_KEY) {
            autoComplete.getSuggestionsAsync(key, type, buffer, this::setSuggestions);
        }
    }


    public void setSuggestions(List<String> suggestions) {
        // update suggestions
        suggestionView.setAdapter(new SuggestionViewAdapter(getSuggestionItemLayout(), suggestions, this::enterSuggestion, getLastWord(), suggestionLeadHighlightColor));


        // highlight predicted keys
        Set<String> characterSuggestions = new HashSet<String>(26);

        if (buffer.isEmpty() || !buffer.endsWith(WORD_SEPARATOR)) {
            int length = buffer.length() - buffer.trim().lastIndexOf(WORD_SEPARATOR) - 1;
            if (length >= 0) {
                suggestions.stream().filter(s -> length < s.length()).map(s -> s.substring(length, length + 1).toUpperCase()).forEach(characterSuggestions::add);
            }
        }

        getLetterKeys().forEach((k, b) -> {
            highlight(mapKey(k), b, characterSuggestions.contains(k));
        });
    }


    @Override
    public void clear() {
        super.clear();
        setSuggestions(emptyList());
    }


    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {

        public String value;
        public TextView view;

        public SuggestionViewHolder(View view, Consumer<String> handler) {
            super(view);
            this.view = (TextView) view.findViewById(R.id.text);

            // enter suggestion on click
            this.view.setOnClickListener(v -> handler.accept(this.value));
        }
    }


    public static class SuggestionViewAdapter extends RecyclerView.Adapter<SuggestionViewHolder> {

        private final List<String> suggestions;
        private final Consumer<String> handler;

        private final String lead;
        private final int leadColor;

        private final int layout;


        public SuggestionViewAdapter(int layout, List<String> suggestions, Consumer<String> handler, String lead, int leadColor) {
            this.layout = layout;
            this.suggestions = suggestions;
            this.handler = handler;
            this.lead = lead;
            this.leadColor = leadColor;
        }

        @Override
        public SuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new SuggestionViewHolder(view, handler);
        }

        @Override
        public void onBindViewHolder(SuggestionViewHolder holder, int position) {
            String word = suggestions.get(position);

            if (lead.length() <= word.length() && lead.equalsIgnoreCase(word.substring(0, lead.length()))) {
                if (lead.endsWith(WORD_SEPARATOR)) {
                    holder.view.setText(word.substring(lead.length()));
                } else {
                    holder.view.setText(highlightLead(word, lead.length()));
                }
            } else {
                holder.view.setText(word);
            }

            // view can be different from suggestion value
            holder.value = word;
        }

        protected Spanned highlightLead(String word, int to) {
            SpannableString span = new SpannableString(word);
            span.setSpan(new ForegroundColorSpan(leadColor), 0, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return span;
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

    }


}
