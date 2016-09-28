package com.gameofthronesonly.adapter;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
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
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.nativead.NativeAdDetails;
import com.startapp.android.publish.nativead.NativeAdPreferences;
import com.startapp.android.publish.nativead.StartAppNativeAd;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by NilayS on 7/6/2016.
 */
public class GalleryAdapter extends BaseListAdapter<QBFile> {

    private SparseArray<QBFile> qbFileSparseArray;
    private DownloadMoreListener downloadListener;
    private int previousGetCount = 0;
    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAd = null;
    private AQuery aq;

    // Declare Native Ad Preferences
    private NativeAdPreferences nativePrefs = new NativeAdPreferences()
            .setAdsNumber(3)                // Load 3 Native Ads
            .setAutoBitmapDownload(true)    // Retrieve Images object
            .setPrimaryImageSize(2);        // 150x150 image

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
        startAppNativeAd = new StartAppNativeAd(context);
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

        if (position % 15 == 0) { //Every 15th item will be an Ad
            // Declare Ad Callbacks Listener
            AdEventListener adListener = new AdEventListener() {     // Callback Listener
                @Override
                public void onReceiveAd(Ad arg0) {
                    // Native Ad received
                    ArrayList<NativeAdDetails> ads = startAppNativeAd.getNativeAds();    // get NativeAds list
                    if (ads.size() > 0) {
                        nativeAd = ads.get(0);
                    }

                    // Verify that an ad was retrieved
                    if (nativeAd != null) {

                        // When ad is received and displayed - we MUST send impression
                        nativeAd.sendImpression(context);
                        if (nativeAd.getImageBitmap() != null) {
                            aq.id(holder.imageView).image(nativeAd.getImageBitmap());
                        } else {
                            aq.id(holder.imageView).image(nativeAd.getImageUrl(), true, true, 0, 0, null, AQuery.FADE_IN);
                        }
                    }
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {
                    // Native Ad failed to receive
                    Log.e("MyApplication", "Error while loading Ad");
                }
            };

            // Load Native Ads
            startAppNativeAd.loadAd(nativePrefs, adListener);
        } else {
            QBFile qbFile = getItem(position);
            loadImage(holder, qbFile);
        }

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