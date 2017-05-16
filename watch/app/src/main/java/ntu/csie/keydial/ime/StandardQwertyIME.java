package ntu.csie.keydial.ime;


import android.content.Context;

import ntu.csie.keydial.AbstractPredictiveKeyboardLayout;
import ntu.csie.keydial.KeyboardLayout;

public class StandardQwertyIME extends AbstractWearIME {


    @Override
    protected AbstractPredictiveKeyboardLayout createKeyboardLayout(Context context) {
        return KeyboardLayout.StandardQwerty.createView(context);
    }


}
