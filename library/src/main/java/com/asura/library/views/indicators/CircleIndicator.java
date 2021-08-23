package com.asura.library.views.indicators;

import android.content.Context;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import com.asura.library.R;

public class CircleIndicator extends IndicatorShape {

    public CircleIndicator(Context context, int indicatorSize, boolean mustAnimateChanges) {
        super(context, indicatorSize, mustAnimateChanges);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_circle_unselected, null));
        } else {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.indicator_circle_unselected));
        }
    }

    @Override
    public void onCheckedChange(boolean isChecked) {
        super.onCheckedChange(isChecked);
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_circle_selected, null));
            } else {
                setBackgroundDrawable(getResources().getDrawable(R.drawable.indicator_circle_selected));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_circle_unselected, null));
            } else {
                setBackgroundDrawable(getResources().getDrawable(R.drawable.indicator_circle_unselected));
            }
        }
    }
}
