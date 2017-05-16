package ntu.csie.keydial.ime;


import android.content.Context;

import ntu.csie.keydial.AbstractPredictiveKeyboardLayout;
import ntu.csie.keydial.KeyboardLayout;

public class SwipeKeyIME extends AbstractWearIME {


    @Override
    protected AbstractPredictiveKeyboardLayout createKeyboardLayout(Context context) {
        return KeyboardLayout.SwipeKeyQwerty.createView(context);
    }


}
