package com.tokopedia.expandable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

/**
 * Created by zulfikarrahman on 2/22/17.
 */

public class ExpandableOptionRadio extends BaseExpandableOptionRadio {

    private AppCompatRadioButton radioButton;

    public ExpandableOptionRadio(Context context) {
        super(context);
    }

    public ExpandableOptionRadio(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableOptionRadio(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExpandableOptionRadio(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        setHeaderLayoutRes(R.layout.item_expandable_option_radio_header);
        super.init();
    }

    @Override
    public CompoundButton getCheckable() {
        if (radioButton == null && getRootView() != null) {
            radioButton = (AppCompatRadioButton) getRootView().findViewById(R.id.radio_button);
        }
        return radioButton;
    }

}
