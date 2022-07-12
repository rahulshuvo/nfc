package com.service.sohan.nfcreadwritetest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.service.sohan.nfcreadwritetest.Api.ApiInterface;
import com.service.sohan.nfcreadwritetest.Api.ApiUtils;
import com.service.sohan.nfcreadwritetest.Model.ProfileResponse;
import com.service.sohan.nfcreadwritetest.Model.VersionResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    String TAG = "PhoneActivityTAG";
    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    boolean writeMode;
    Tag myTag;
    Context context;
    int currentVersion = 0;
    TextView tvNFCContent;
    EditText message, name, address, phone;
    Button btnWrite, reader, writer;
    ImageView logo;
    private CompositeDisposable mCompositeDisposable;
    ApiInterface apiInterface;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentVersion = BuildConfig.VERSION_CODE;

        reader = (Button) findViewById(R.id.btnReader);
        reader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ReadActivity.class));
            }
        });

        writer = (Button) findViewById(R.id.btnWriter);
        writer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), WriteActivity.class));
            }
        });

        logo = (ImageView)findViewById(R.id.imgLogo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            //finish();
        }

        mCompositeDisposable = new CompositeDisposable();
        apiInterface = ApiUtils.getService();
        /*getVersion();*/
    }

    /*public void getVersion(){
        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(MainActivity.this, "", "Data Loading. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.getVersion() // while release give user id
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseVersion, this::handleErrorVersion));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getVersion();
                }
            });
            ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            ad.setTitle("Error");
            ad.setMessage("No Internet connection");
            ad.show();
        }

    }*/


    private void handleResponseVersion(VersionResponse clientResponse) {
        dialog.dismiss();

        if(currentVersion <= Integer.parseInt(clientResponse.getVersion().get(0).getAndVerCode())){
            if(clientResponse.getVersion().get(0).getAndMustUpdate())
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage(clientResponse.getVersion().get(0).getAndVerNote());
                alertBuilder.setTitle("New version available.");
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("Update",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(clientResponse.getVersion().get(0).getAndLink()));
                        startActivity(browse);
                        finish();
                        //System.exit(0);
                    }
                });
                alertBuilder.setNegativeButton("Close Application", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertBuilder.show();
            }
            else if(!clientResponse.getVersion().get(0).getAndMustUpdate()) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage(clientResponse.getVersion().get(0).getAndVerNote());
                alertBuilder.setTitle("New version available");
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(clientResponse.getVersion().get(0).getAndLink()));
                        startActivity(browse);
                        finish();
                    }
                });
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertBuilder.show();
            }
        }else {

        }

    }

    private void handleErrorVersion(Throwable error) {
        dialog.dismiss();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }

}
