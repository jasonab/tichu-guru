package kankan.wheel.widget.adapters;

import android.content.Context;

/* loaded from: classes.dex */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {
    public static final int DEFAULT_MAX_VALUE = 9;
    private static final int DEFAULT_MIN_VALUE = 0;
    private String format;
    private int maxValue;
    private int minValue;

    public NumericWheelAdapter(Context context) {
        this(context, 0, 9);
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    @Override // kankan.wheel.widget.adapters.AbstractWheelTextAdapter
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = this.minValue + index;
            return this.format != null ? String.format(this.format, Integer.valueOf(value)) : Integer.toString(value);
        }
        return null;
    }

    @Override // kankan.wheel.widget.adapters.WheelViewAdapter
    public int getItemsCount() {
        return (this.maxValue - this.minValue) + 1;
    }
}
