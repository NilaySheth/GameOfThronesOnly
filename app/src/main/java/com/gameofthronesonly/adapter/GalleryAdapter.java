package com.gameofthronesonly.adapter;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.gameofthronesonly.R;
import com.gameofthronesonly.constants.BaseListAdapter;
import com.gameofthronesonly.constants.DownloadMoreListener;
import com.quickblox.content.model.QBFile;

import java.io.File;

/**
 * Created by NilayS on 7/6/2016.
 */
public class GalleryAdapter extends BaseListAdapter<QBFile> {

    private SparseArray<QBFile> qbFileSparseArray;
    private DownloadMoreListener downloadListener;
    private int previousGetCount = 0;
    private AQuery aq;

    public GalleryAdapter(Context context, SparseArray<QBFile> qbFileSparseArray) {
        super(context);
        this.qbFileSparseArray = qbFileSparseArray;
        File ext = Environment.getExternalStorageDirectory();
        File cacheDir = new File(ext, "GOT");
        AQUtility.setCacheDir(cacheDir);
        AjaxCallback.setNetworkLimit(25);
        BitmapAjaxCallback.setIconCacheLimit(200);
        BitmapAjaxCallback.setCacheLimit(200);
        aq = new AQuery(context);
    }

    @Override
    public int getCount() {
        return qbFileSparseArray.size();
    }

    @Override
    public QBFile getItem(int position) {
        return qbFileSparseArray.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return qbFileSparseArray.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_gallery, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_preview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        QBFile qbFile = getItem(position);
        loadImage(holder, qbFile);

        downloadMore(position);
        return convertView;
    }

    private void downloadMore(int position) {
        int count = getCount();
        if (count - 1 == position) {
            if (count != previousGetCount) {
                downloadListener.downloadMore();
                previousGetCount = count;
            }
        }
    }

    public void updateData(SparseArray<QBFile> qbFileSparseArray) {
        this.qbFileSparseArray = qbFileSparseArray;
        notifyDataSetChanged();
    }

    private void loadImage(final ViewHolder holder, QBFile qbFile) {
        if (aq.getCachedImage(getUrl(qbFile)) != null) {
            aq.id(holder.imageView).image(aq.getCachedImage(getUrl(qbFile)));
        } else {
            aq.id(holder.imageView).image(getUrl(qbFile), true, true, 0, 0, null, AQuery.FADE_IN);
        }
    }

    public static String getUrl(QBFile qbFile) {
        if (qbFile.isPublic()) {
            String publicUrl = qbFile.getPublicUrl();
            if (!TextUtils.isEmpty(publicUrl)) {
                return publicUrl;
            }
        }
        return qbFile.getPrivateUrl();
    }

    public void setDownloadMoreListener(DownloadMoreListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    private static class ViewHolder {
        private ImageView imageView;
    }
}