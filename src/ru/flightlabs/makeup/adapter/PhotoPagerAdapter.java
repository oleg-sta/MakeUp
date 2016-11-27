package ru.flightlabs.makeup.adapter;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoPagerAdapter extends PagerAdapter {

    private Activity _activity;
    public List<String> photos;
    
    public PhotoPagerAdapter(Activity activity, List<String> photos) {
        _activity = activity;
        this.photos = photos;
    }
    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }
    
    public Object instantiateItem(ViewGroup container, int position) {
//        LayoutInflater inflater = (LayoutInflater) _activity
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView imageView = new ImageView(_activity);
        
        final BitmapFactory.Options options2 = new BitmapFactory.Options();
        //options2.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(photos.get(position), options2);
        //imageView.setImage(ImageSource.uri(photoPath));
        imageView.setImageBitmap(bm);

        ((ViewPager) container).addView(imageView);
        return imageView;
    }
    
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
  
    }
    

    @Override
    public int getItemPosition(Object object) {
        if (photos.contains((View) object)) {
            return photos.indexOf((View) object);
        } else {
            return POSITION_NONE;
        }
    }

}
