package kankan.wheel.widget.adapters;

import android.content.Context;

/* loaded from: classes.dex */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter {
    private T[] items;

    public ArrayWheelAdapter(Context context, T[] tArr) {
        super(context);
        this.items = tArr;
    }

    @Override // kankan.wheel.widget.adapters.AbstractWheelTextAdapter
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < this.items.length) {
            T item = this.items[index];
            if (item instanceof CharSequence) {
                return (CharSequence) item;
            }
            return item.toString();
        }
        return null;
    }

    @Override // kankan.wheel.widget.adapters.WheelViewAdapter
    public int getItemsCount() {
        return this.items.length;
    }
}
