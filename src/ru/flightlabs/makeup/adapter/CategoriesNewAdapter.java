package ru.flightlabs.makeup.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.flightlabs.masks.utils.BitmapLibs;
import ru.oramalabs.beautykit.R;

/**
 * Created by sov on 10.02.2017.
 */

public class CategoriesNewAdapter extends BaseAdapter {
    AdaptersNotifier fdAct;
    Context mContext;
    TypedArray images;
    LayoutInflater mLayoutInflater;
    public int selected;
    String[] names;

    public CategoriesNewAdapter(AdaptersNotifier context, TypedArray images, String[] names) {
        mContext = (Context)context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
        this.fdAct = context;
        this.names = names;
    }

    public int getCount() {
        return images.length();
    }

    public Object getItem(int position) {
        return images.getResourceId(position, 0);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        // Not using convertView for sample app simplicity. You should probably use it in real application to get better performance.
        View itemView = mLayoutInflater.inflate(R.layout.item_effect, null, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
        if (names != null) {
            ((TextView) itemView.findViewById(R.id.item_text)).setText(names[position]);
        }
        int resourceId = images.getResourceId(position, 0);
        if (images.getString(position).contains(";")) {
            resourceId = mContext.getResources().getIdentifier(images.getString(position).split(";")[0], "raw", mContext.getPackageName());
        }
        Bitmap bm = BitmapLibs.getSampledResource(mContext, resourceId);
        imageView.setImageBitmap(bm);
        //imageView.setBackgroundColor(Color.WHITE);
        return itemView;
    }
}
