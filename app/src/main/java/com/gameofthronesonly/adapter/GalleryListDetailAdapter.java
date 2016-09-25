package com.gameofthronesonly.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.gameofthronesonly.R;
import com.gameofthronesonly.constants.DownloadMoreListener;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by NilayS on 8/13/2016.
 */
public class GalleryListDetailAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private DownloadMoreListener downloadListener;
    private SparseArray<QBFile> qbFileSparseArray;
    private int previousGetCount = 0;
    private AQuery aq;

    public GalleryListDetailAdapter(Context context, SparseArray<QBFile> qbFileSparseArray) {
        this.context = context;
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
    public Object getItem(int location) {
        return qbFileSparseArray.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (inflater == null)
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_detail, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_full_view);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.download = (ImageView) convertView.findViewById(R.id.download);
            holder.share = (ImageView) convertView.findViewById(R.id.share);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (aq.getCachedImage(getUrl(qbFileSparseArray.valueAt(position))) != null) {
            aq.id(holder.imageView).image(aq.getCachedImage(getUrl(qbFileSparseArray.valueAt(position))));
        } else {
            aq.id(holder.imageView).image(getUrl(qbFileSparseArray.valueAt(position)), true, true, 0, 0, null, AQuery.FADE_IN);
        }

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fileUID = qbFileSparseArray.valueAt(position).getUid();
                downloadListener.downloadFile(fileUID);
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(position);
            }
        });

        holder.image.setVisibility(View.GONE);
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

    private void shareImage(int position) {

    }

    public void updateData(SparseArray<QBFile> qbFileSparseArray) {
        this.qbFileSparseArray = qbFileSparseArray;
        notifyDataSetChanged();
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
        private ImageView imageView, image, share, download;
    }
}