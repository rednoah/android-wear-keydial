package ntu.csie.keydial;


import android.content.Context;

public enum KeyboardLayout {


    KeyDialAlpha {
        @Override
        KeyDial createView(Context context) {
            return new KeyDial(context, KeyDial.ALPHA);
        }
    },


    KeyDialAlphaZigZag {
        @Override
        KeyDial createView(Context context) {
            return new KeyDial(context, KeyDial.ALPHA_ZIGZAG);
        }
    },


    KeyDialQwertyZigZag {
        @Override
        KeyDial createView(Context context) {
            return new KeyDial(context, KeyDial.QWERTY_ZIGZAG);
        }
    },


    StandardQwerty {
        @Override
        StandardQwerty createView(Context context) {
            return new StandardQwerty(context);
        }
    },


    SwipeKeyAlpha {
        @Override
        SwipeKey createView(Context context) {
            return new SwipeKey(context, SwipeKey.ALPHA);
        }
    },


    SwipeKeyQwerty {
        @Override
        SwipeKey createView(Context context) {
            return new SwipeKey(context, SwipeKey.QWERTY);
        }
    };


    abstract AbstractPredictiveKeyboardLayout createView(Context context);


    int getIcon() {
        switch (this) {
            case KeyDialAlpha:
                return R.mipmap.keydial_alpha;
            case KeyDialAlphaZigZag:
                return R.mipmap.keydial_alpha_zigzag;
            case KeyDialQwertyZigZag:
                return R.mipmap.keydial_qwerty_zigzag;
            case StandardQwerty:
                return R.mipmap.standard_qwerty;
            case SwipeKeyAlpha:
                return R.mipmap.swipekey_alpha;
            case SwipeKeyQwerty:
                return R.mipmap.swipekey_qwerty;
            default:
                return R.mipmap.ic_launcher;
        }
    }


}
