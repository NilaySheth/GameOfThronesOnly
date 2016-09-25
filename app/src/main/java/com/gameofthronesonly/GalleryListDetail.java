package com.gameofthronesonly;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.androidquery.callback.BitmapAjaxCallback;
import com.gameofthronesonly.adapter.GalleryListDetailAdapter;
import com.gameofthronesonly.constants.DataHolder;
import com.gameofthronesonly.constants.DownloadMoreListener;
import com.gameofthronesonly.constants.ErrorUtils;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;

import java.io.BufferedOutputStream;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by NilayS on 8/1/2016.
 */
public class GalleryListDetail extends AppCompatActivity implements DownloadMoreListener {
    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Movies json url
    private ListView listView;
    private GalleryListDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new GalleryListDetailAdapter(getApplicationContext(), DataHolder.getInstance().getQBFiles());
        adapter.setDownloadMoreListener(this);
        listView.setAdapter(adapter);

        if(getIntent().hasExtra("counter")){
            final int counter = getIntent().getIntExtra("counter", 0);
            listView.setSelection(counter);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                onBackPressed();
            }
        });
    }

    private void getFileList() {

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(MainActivity.IMAGES_PER_PAGE);
        builder.setPage(MainActivity.current_page++);

        QBContent.getFiles(builder, new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                if (qbFiles.isEmpty()) {
                    MainActivity.current_page--;
                } else {
                    DataHolder.getInstance().addQbFiles(qbFiles);
                }
//                if (progressDialog.isIndeterminate()) {
//                    progressDialog.dismiss();
//                }
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
//                progressDialog.dismiss();
                MainActivity.current_page--;
                View view = findViewById(R.id.activity_gallery);
                showSnackbarError(view, R.string.splash_create_session_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFileList();
                    }
                });
            }
        });
    }

    private void updateData() {
        adapter.updateData(DataHolder.getInstance().getQBFiles());
    }

    @Override
    public void downloadMore() {
        getFileList();
    }

    @Override
    public void downloadFile(final String fileUID) {
        QBContent.downloadFile(fileUID, new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(final InputStream inputStream, Bundle params) {
                long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                Log.i("", "content.length: " + length);

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while(true) {
                                File sdCard = Environment.getExternalStorageDirectory();
                                File dir = new File(sdCard.getAbsolutePath() + "/GameOfThronesOnly");
                                dir.mkdirs();
                                File file = new File(dir, fileUID + ".jpg");
                                OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    stream.write(buffer, 0, len);
                                }
                                if(stream != null) {
                                    stream.close();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "Image saved successfully on this device", Toast.LENGTH_SHORT).show();
                    }
                };
                thread.start();
            }

            @Override
            public void onError(QBResponseException errors) {

            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                Log.i("Download File", "progress: " + progress);
            }
        });
    }

    protected void showSnackbarError(View rootLayout, @StringRes int resId, QBResponseException e, View.OnClickListener clickListener) {
        ErrorUtils.showSnackbar(rootLayout, resId, e, R.string.dlg_retry, clickListener);
    }

    @Override
    public void onLowMemory(){
        BitmapAjaxCallback.clearCache();
    }
}
