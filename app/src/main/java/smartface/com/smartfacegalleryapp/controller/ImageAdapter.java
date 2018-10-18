package smartface.com.smartfacegalleryapp.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import smartface.com.smartfacegalleryapp.R;
import smartface.com.smartfacegalleryapp.model.NYTimesImage;

public class ImageAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<NYTimesImage> images;

    // 1
    public ImageAdapter(Context context, ArrayList<NYTimesImage> images) {
        this.mContext = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final NYTimesImage image = images.get(position);

        // view holder pattern
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.layout_image, null);

            final ImageView imageView = convertView.findViewById(R.id.ivNYtimesImage);

            final ViewHolder viewHolder = new ViewHolder(imageView);
            convertView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        //Using Picasso API to fill imageView with the given imageURL.
        Picasso.with(mContext).load(image.getUrl()).into(viewHolder.imageView);

        return convertView;
    }

    private class ViewHolder {
        private final ImageView imageView;

        public ViewHolder(ImageView imageView) {
            this.imageView = imageView;
        }
    }

}
