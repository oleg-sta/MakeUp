package ru.flightlabs.makeup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import ru.oramalabs.beautykit.R;


/**
 * Created by sov on 16.04.2017.
 */

public class ColorsNewPagerAdapter extends BaseAdapter {
    AdaptersNotifier fdAct;
    Context mContext;
    LayoutInflater mLayoutInflater;
    public int selected;
    int[] colors;

    public ColorsNewPagerAdapter(AdaptersNotifier context, int[] colors) {
        mContext = (Context)context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fdAct = context;
        this.colors = colors;
    }

    public int getCount() {
        return colors.length;
    }

    public Object getItem(int position) {
        return colors[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        // Not using convertView for sample app simplicity. You should probably use it in real application to get better performance.
        View itemView = mLayoutInflater.inflate(R.layout.item_effect, null, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
        if (position == 0) {
            imageView.setImageAlpha(20);
        }
        imageView.setImageResource(R.drawable.empty_icon_00);
        imageView.setColorFilter(colors[position]);
        return itemView;
    }
}

