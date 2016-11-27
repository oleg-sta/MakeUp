package ru.flightlabs.makeup;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilterPagerAdapter extends PagerAdapter {

    CommonI fdAct;
    Context mContext;
    TypedArray images;
    LayoutInflater mLayoutInflater;

    public FilterPagerAdapter(CommonI context, TypedArray images) {
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

        imageView.setImageResource(R.drawable.color_picker);
//        imageView.setImageResource(images.getResourceId(position, 0));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), images.getResourceId(position, 0), options);
        options.inSampleSize = calculateInSampleSize(options, 60, 60);
        // FIXME
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), images.getResourceId(position, 0), options);
        imageView.setImageBitmap(bm);

        imageView.setBackgroundColor(Color.WHITE);
        container.addView(itemView);
        itemView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                fdAct.changeMask(position);
                
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

    // TODO in library
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
    }
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}