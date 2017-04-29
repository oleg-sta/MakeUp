package ru.flightlabs.makeup.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ru.flightlabs.makeup.activity.ActivityMakeUp;
import ru.oramalabs.beautykit.R;

/**
 * Created by sov on 26.04.2017.
 */

public class TextNewPagerAdapter extends BaseAdapter {
    public int selected;
    private String[] textNames;
    LayoutInflater mLayoutInflater;
    public TextView current;
    Context mContext;

    public TextNewPagerAdapter(ActivityMakeUp activityMakeUp, String[] textNames) {
        mLayoutInflater = (LayoutInflater) activityMakeUp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.textNames = textNames;
        mContext = activityMakeUp;
    }

    @Override
    public int getCount() {
        return textNames.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View itemView = mLayoutInflater.inflate(R.layout.item_text, null, false);
        TextView imageView = (TextView) itemView.findViewById(R.id.item_text);
        imageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // FIXME resource
        if (position == selected) {
            imageView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
            current = imageView;
        } else {
            imageView.setTextColor(mContext.getResources().getColor(R.color.main_text));
        }
        imageView.setText(textNames[position]);
        return itemView;
    }
}
