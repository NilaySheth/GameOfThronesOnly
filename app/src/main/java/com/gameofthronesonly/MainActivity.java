package com.gameofthronesonly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.androidquery.callback.BitmapAjaxCallback;
import com.gameofthronesonly.adapter.GalleryAdapter;
import com.gameofthronesonly.constants.Consts;
import com.gameofthronesonly.constants.DataHolder;
import com.gameofthronesonly.constants.DialogUtils;
import com.gameofthronesonly.constants.DownloadMoreListener;
import com.gameofthronesonly.constants.ErrorUtils;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, DownloadMoreListener {

    protected ProgressDialog progressDialog;
    public static final int IMAGES_PER_PAGE = 100;
    public static int current_page = 1;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initUI();
        createSession();
    }

    private void initUI() {
        galleryAdapter = new GalleryAdapter(this, DataHolder.getInstance().getQBFiles());
        galleryAdapter.setDownloadMoreListener(this);

        GridView galleryGridView = (GridView) findViewById(R.id.gallery_gridview);
        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QBFile qbFile = (QBFile) adapterView.getItemAtPosition(position);
        Intent intent = new Intent(getApplicationContext(), GalleryListDetail.class);
        intent.putExtra("qid", qbFile.getId());
        intent.putExtra("counter", position);
        startActivity(intent);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void createSession() {

        if(isNetworkAvailable(getApplicationContext())){
            progressDialog = DialogUtils.getProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);
            QBAuth.createSession(qbUser, new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {
                    getFileList();
                }

                @Override
                public void onError(QBResponseException e) {
                    View view = findViewById(R.id.activity_gallery);
                    showSnackbarError(view, R.string.splash_create_session_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createSession();
                        }
                    });
                }
            });
        }else{
            View view = findViewById(R.id.activity_gallery);
            Snackbar.make(view, R.string.no_internet_connection, Snackbar.LENGTH_LONG)
                    .setAction(R.string.dlg_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createSession();
                        }
                    }).show();
        }

    }

    private void getFileList() {

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(IMAGES_PER_PAGE);
        builder.setPage(current_page++);

        QBContent.getFiles(builder, new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                if (qbFiles.isEmpty()) {
                    current_page--;
                } else {
                    DataHolder.getInstance().addQbFiles(qbFiles);
                }
                if (progressDialog.isIndeterminate()) {
                    progressDialog.dismiss();
                }
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                current_page--;
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
        galleryAdapter.updateData(DataHolder.getInstance().getQBFiles());
    }

    protected void showSnackbarError(View rootLayout, @StringRes int resId, QBResponseException e, View.OnClickListener clickListener) {
        ErrorUtils.showSnackbar(rootLayout, resId, e, R.string.dlg_retry, clickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about_us) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void downloadMore() {
        getFileList();
    }

    @Override
    public void downloadFile(String fileId) {

    }

    @Override
    public void onLowMemory(){
        BitmapAjaxCallback.clearCache();
    }
}
