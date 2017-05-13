package ntu.csie.keydial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CurvedChildLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.function.Consumer;


public class MainActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableRecyclerView keyboardRecycler = (WearableRecyclerView) findViewById(R.id.keyboardRecycler);
        keyboardRecycler.setHasFixedSize(true);
        keyboardRecycler.setCenterEdgeItems(true);

        keyboardRecycler.setLayoutManager(new CurvedChildLayoutManager(getApplicationContext()));
        keyboardRecycler.setAdapter(new KeyboardItemAdapter(getKeyboardLayouts(), this::openKeyboard));

        // make sure that screen doesn't turn off during user study
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void openKeyboard(KeyboardLayout keyboard) {
        if (keyboard == null) {
            for (File f : getApplicationContext().getFilesDir().listFiles()) {
                try {
                    Log.d("RESET", "Delete " + f);
                    FileUtils.forceDelete(f);
                } catch (Exception e) {
                    Log.d("RESET", "RESET FAILED", e);
                }
            }
            finish();
            System.exit(0); // exit process to reset Adaptext learning language model
        }


        Log.d("MainActivity", "Open Keyboard: " + keyboard);

        Intent intent = new Intent(this, UserStudyActivity.class);
        intent.putExtra(UserStudyActivity.EXTRA_KEYBOARD_LAYOUT, keyboard.name());
        intent.putExtra(UserStudyActivity.EXTRA_PHRASE_COUNT, getPhraseCount());

        startActivity(intent);
    }


    public KeyboardLayout[] getKeyboardLayouts() {
        return UserStudyActivity.MAIN_SESSION_KEYBOARD_LAYOUTS;
    }


    public int getPhraseCount() {
        return UserStudyActivity.MAIN_SESSION_PHRASE_COUNT;
    }


    public static class KeyboardItem extends RecyclerView.ViewHolder {

        private KeyboardLayout keyboard;
        private TextView text;


        public KeyboardItem(ViewGroup parent, Consumer<KeyboardLayout> handler) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keyboard, parent, false));

            this.text = (TextView) itemView.findViewById(R.id.text);
            this.text.setOnClickListener(v -> handler.accept(keyboard));
        }


        public void setValue(KeyboardLayout keyboard) {
            if (keyboard != null) {
                this.keyboard = keyboard;
                this.text.setText(keyboard.toString());
                this.text.setCompoundDrawablesWithIntrinsicBounds(keyboard.getIcon(), 0, 0, 0);
            } else {
                this.keyboard = null;
                this.text.setText("RESET");
                this.text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cc_clear, 0, 0, 0);
            }
        }
    }


    public static class KeyboardItemAdapter extends RecyclerView.Adapter<KeyboardItem> {

        private KeyboardLayout[] layouts;
        private Consumer<KeyboardLayout> handler;


        public KeyboardItemAdapter(KeyboardLayout[] layouts, Consumer<KeyboardLayout> handler) {
            this.layouts = layouts;
            this.handler = handler;
        }

        @Override
        public KeyboardItem onCreateViewHolder(ViewGroup parent, int viewType) {
            return new KeyboardItem(parent, handler);
        }

        @Override
        public void onBindViewHolder(KeyboardItem holder, int position) {
            try {
                holder.setValue(layouts[position]);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.setValue(null);
            }
        }

        @Override
        public int getItemCount() {
            return layouts.length + 1;
        }


    }


}