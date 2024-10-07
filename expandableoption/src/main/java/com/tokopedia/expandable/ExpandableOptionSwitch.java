package com.tokopedia.expandable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Created by zulfikarrahman on 4/6/17.
 */

public class ExpandableOptionSwitch extends BaseExpandableOptionText {

    private SwitchCompat switchCompat;

    public ExpandableOptionSwitch(Context context) {
        super(context);
    }

    public ExpandableOptionSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableOptionSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExpandableOptionSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setExpand(isChecked);
            }
        });
        switchCompat.setChecked(isExpanded());
        super.onFinishInflate();
    }

    @Override
    protected void init() {
        setHeaderLayoutRes(R.layout.item_expandable_option_switch_header);
        super.init();
    }

    @Override
    protected void initView(View view) {
        switchCompat = (SwitchCompat) view.findViewById(R.id.switch_button);
        super.initView(view);
    }

    @Override
    public void setExpand(boolean isChecked) {
        switchCompat.setChecked(isChecked);
        super.setExpand(isChecked);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        switchCompat.setEnabled(enabled);
        switchCompat.setClickable(enabled);
    }
}
