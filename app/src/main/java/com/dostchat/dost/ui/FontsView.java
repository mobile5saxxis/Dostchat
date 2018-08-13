package com.dostchat.dost.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dostchat.dost.R;

/**
 * Created by Abderrahim on 11/5/2015.
 * This class to create icon by font awesome
 */
public class FontsView extends android.support.v7.widget.AppCompatTextView {


    public FontsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public FontsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        try {
            TypedArray tA = context.obtainStyledAttributes(attrs, R.styleable.FontsView);

            String font = tA.getString(R.styleable.FontsView_fontValue);



            String fontPath = String.format("fonts/%s", font);


            Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), fontPath);
            setTypeface(myTypeface);
            tA.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
