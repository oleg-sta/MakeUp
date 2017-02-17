package ru.flightlabs.makeup.adapter;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ru.flightlabs.makeup.R;
import ru.flightlabs.masks.utils.BitmapLibs;

public class CategoriesPagerAdapter extends PagerAdapter {

    AdaptersNotifier fdAct;
    Context mContext;
    TypedArray images;
    LayoutInflater mLayoutInflater;

    public CategoriesPagerAdapter(AdaptersNotifier context, TypedArray images) {
        mContext = (Context)context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
        this.fdAct = context;
    }

    @Override
    public int getCount() {
        return images.length();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.item_effect, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
        Bitmap bm = BitmapLibs.getSampledResource(mContext, images.getResourceId(position, 0));
        imageView.setImageBitmap(bm);
        imageView.setBackgroundColor(Color.WHITE);
        container.addView(itemView);
        itemView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                fdAct.changeItemInCategory(position);
            }
        });
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
    
    @Override
    public float getPageWidth(int position) {
        return 1f / 4;
    }

 }