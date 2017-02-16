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

import ru.flightlabs.makeup.CommonI;

import ru.flightlabs.makeup.R;
import ru.flightlabs.masks.utils.BitmapLibs;

/**
 * Created by sov on 10.02.2017.
 */

public class CategoriesNewAdapter extends BaseAdapter {
    CommonI fdAct;
    Context mContext;
    TypedArray images;
    LayoutInflater mLayoutInflater;
    public int selected;

    public CategoriesNewAdapter(CommonI context, TypedArray images) {
        mContext = (Context)context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
        this.fdAct = context;
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
        Bitmap bm = BitmapLibs.getSampledResource(mContext, images.getResourceId(position, 0));
        imageView.setImageBitmap(bm);
        imageView.setBackgroundColor(Color.WHITE);
        if (position == selected) {
            itemView.findViewById(R.id.item_image_border).setVisibility(View.VISIBLE);
        }
//        itemView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                fdAct.changeItemInCategory(position);
//            }
//        });;
        return itemView;
    }
}
