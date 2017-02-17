package ru.flightlabs.makeup.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.flightlabs.makeup.R;


/**
 * Created by sov on 19.11.2016.
 */

public class CategoriesNamePagerAdapter extends PagerAdapter {

    Notification notification;
    Context mContext;
    String[] texts;
    LayoutInflater mLayoutInflater;
    public int selected = 3;
    TextView textViewPrevious;

    public CategoriesNamePagerAdapter(Notification context, String[] texts) {
        // something terrible
        mContext = (Context)context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.notification = context;
        this.texts = texts;
    }

    @Override
    public int getCount() {
        return texts.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.category, container, false);
        final TextView textView = (TextView) itemView.findViewById(R.id.item_text);
        textView.setText(texts[position]);
        if (selected == position) {
            textView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
            textViewPrevious = textView;
        }
        container.addView(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notification.selectedCategory(position);
                selected = position;
                if (textViewPrevious != null) {
                    textViewPrevious.setTextColor(mContext.getResources().getColor(R.color.main_text));
                }
                textView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
                textViewPrevious = textView;
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

    public interface Notification {
        void selectedCategory(int position);
    }
}