package com.tokopedia.expandable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;

/**
 * Created by zulfikarrahman on 2/22/17.
 */

public abstract class BaseExpandableOptionRadio extends BaseExpandableOption {

    public BaseExpandableOptionRadio(Context context) {
        super(context);
    }

    private OnRadioCheckChangedListener onRadioCheckChangedListener;

    public interface OnRadioCheckChangedListener {
        void onCheckChangedListener(BaseExpandableOptionRadio expandableOptionRadio,
                                    int id, boolean isChecked);
    }

    public void setOnRadioCheckChangedListener(OnRadioCheckChangedListener onRadioCheckChangedListener) {
        this.onRadioCheckChangedListener = onRadioCheckChangedListener;
    }

    public BaseExpandableOptionRadio(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseExpandableOptionRadio(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseExpandableOptionRadio(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initView(View view) {
        CompoundButton checkable = getCheckable();
        if (checkable != null) {
            getCheckable().setText(titleText);
            checkable.setChecked(isExpanded());
            checkable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onHeaderClicked();
                }
            });
        }
        setExpand(isExpanded());
    }

    public abstract CompoundButton getCheckable();

    private void onRadioClicked() {
        setExpand(true);
        if (onRadioCheckChangedListener != null) {
            onRadioCheckChangedListener.onCheckChangedListener(this, getId(), true);
        }
    }

    @Override
    protected void onHeaderClicked() {
        if (!isExpanded()) {
            onRadioClicked();
        }
    }

    public void setExpand(boolean b) {
        if (b != isExpanded()) {
            CompoundButton compoundButton = getCheckable();
            if (compoundButton != null) {
                compoundButton.setChecked(b);
            }
        }
        super.setExpand(b);
    }

    @Override
    public void setTitleText(String titleText) {
        if (getCheckable() != null) {
            getCheckable().setText(titleText);
        }
        super.setTitleText(titleText);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (getCheckable() != null) {
            getCheckable().setEnabled(enabled);
        }
    }
}
