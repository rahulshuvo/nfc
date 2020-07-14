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
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.service.sohan.nfcreadwritetest.Api.ApiInterface;
import com.service.sohan.nfcreadwritetest.Api.ApiUtils;
import com.service.sohan.nfcreadwritetest.Model.CategoryResponse;
import com.service.sohan.nfcreadwritetest.Model.ContactTypeResponse;
import com.service.sohan.nfcreadwritetest.Model.ProfileResponse;
import com.service.sohan.nfcreadwritetest.Model.ReaderDataPostedResponse;
import com.service.sohan.nfcreadwritetest.Model.StateResponse;
import com.service.sohan.nfcreadwritetest.Model.WriterPostedResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener{
    private final String TAG = "nfc_"+this.getClass().getSimpleName();
    //String TAG = "PhoneActivityTAGReader";
    Activity activity = ReadActivity.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    ArrayList<String> _mst=new ArrayList<>();

    public static final String MIME_TEXT_PLAIN = "text/plain";
    TextView readTag, tvVersion;
    Tag myTag;
    NfcAdapter nfcAdapter;
    Button btnExit, btnWrite, btnClear;
    EditText name, cell, lastname, address, city, state, email, workNumber, company, devicePhone, deviceOwnerName, workNumberEnterprise, faxNumber, websiteLink, realPhone, otherInfo, etcontactType, categoryType, etTitle, etAddress2, region, zipcode, memo;
    boolean contactListSelected = false;
    boolean webSelected = false;
    private CompositeDisposable mCompositeDisposable;
    ApiInterface apiInterface;
    ProgressDialog dialog;
    String phoneNumber, sharedValue;
    ImageView logo;
    AutoCompleteTextView comClient;
    ArrayAdapter<String> sourceAdapter;
    String companyCode, category, contactType;
    LinearLayout llMain;
    List<StateResponse.Data> stateList;
    List<ContactTypeResponse.Data> contactTypeList;
    List<CategoryResponse.Data> categoryList;
    //    List<String> categoryList;
//    List<String> typeList;
    Spinner spinCat, spinTyp;
    ArrayAdapter adapterCat, adapterTyp;
    String stateValue = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        //readTag = (TextView)findViewById(R.id.nfc_contents);
        tvVersion = (TextView)findViewById(R.id.tvVersion);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        tvVersion.setText("Version: "+versionName);

        llMain = (LinearLayout)findViewById(R.id.llMain);
        stateList = new ArrayList<>();
        categoryList = new ArrayList<>();
        contactTypeList = new ArrayList<>();
        readFromIntent(getIntent());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }

        btnExit = (Button)findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        logo = (ImageView)findViewById(R.id.imgLogo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
            }
        });

        btnWrite = (Button)findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()==null){
                    if(contactListSelected && webSelected){
                        sharedValue = "0";
                        postReaderData(sharedValue);
                    }
                    else if (webSelected) {
                        sharedValue = "1";
                        postReaderData(sharedValue);
                        //Toast.makeText(getApplicationContext(), "Web called", Toast.LENGTH_LONG).show();
                    } else if (contactListSelected) {
                        //Toast.makeText(getApplicationContext(), "Contacts called", Toast.LENGTH_LONG).show();
                        saveContacts();
                        sharedValue = "0";
                        postReaderData(sharedValue);
                    } else {
                        sharedValue = "0";
                        postReaderData(sharedValue);
//                        AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
//                        ad.setCancelable(false);
//                        ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //postReaderData();
//                            }
//                        });
//                        ad.setTitle("Error");
//                        ad.setMessage("Please select from saving option, Contact list, Web or both?");
//                        ad.show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), validate(), Toast.LENGTH_LONG).show();
                }
            }
        });

        name = (EditText)findViewById(R.id.etFirstName);
        lastname = (EditText)findViewById(R.id.etLastName);
        cell = (EditText)findViewById(R.id.etCell);
//        cell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    //Toast.makeText(getApplicationContext(), "Got the focus", Toast.LENGTH_LONG).show();
//                } else {
//                    // Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
//                    getUser();
//                }
//            }
//        });

        spinCat = (Spinner)findViewById(R.id.spinCategory);
        spinCat.setOnItemSelectedListener(this);
        adapterCat = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterCat.add("Select Category");
        adapterCat.addAll(categoryList);
        spinCat.setAdapter(adapterCat);

        spinTyp = (Spinner)findViewById(R.id.spinContactType);
        spinTyp.setOnItemSelectedListener(this);
        adapterTyp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
        adapterTyp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterTyp.add("Select Contact Type");
        adapterTyp.addAll(contactTypeList);
        spinTyp.setAdapter(adapterTyp);

//        comClient = (AutoCompleteTextView)findViewById(R.id.comSymbol);
//        comClient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (comClient.getText().length() > 0) {
//
//                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(llMain.getWindowToken(), 0);
//
//                    //Toast.makeText(getApplicationContext(), "selected "+comClient.getText().toString(), Toast.LENGTH_LONG).show();
////                    String b4companyCode = comClient.getText().toString();
////                    String[] parts = b4companyCode.split("-");
////                    String code = parts[0]; // 004
////                    companyCode = code;
//
//                    String Code = comClient.getText().toString();
//                    companyCode = Code;
//                    int index = getIndex(companyCode);
//                    stateValue = stateList.get(index).getState();
//                    //Toast.makeText(getApplicationContext(), "selected state "+stateValue, Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Please select a company", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//        sourceAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
//
//        comClient.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                comClient.showDropDown();
//            }
//        });

        address = (EditText)findViewById(R.id.etAddress);
        city = (EditText)findViewById(R.id.etCity);
        state = (EditText)findViewById(R.id.etState);
        email = (EditText)findViewById(R.id.etEmail);
        workNumber = (EditText)findViewById(R.id.etWorkNumber);
        company = (EditText)findViewById(R.id.etCompany);
        devicePhone = (EditText)findViewById(R.id.etDevicePhone);
        deviceOwnerName = (EditText)findViewById(R.id.etDeviceOwnerName);
        workNumberEnterprise = (EditText)findViewById(R.id.etWorkNumberEnterprise);
        faxNumber = (EditText)findViewById(R.id.etFaxNumber);
        websiteLink = (EditText)findViewById(R.id.etWebsiteLink);
        realPhone = (EditText)findViewById(R.id.etRealPhone);
        otherInfo = (EditText)findViewById(R.id.etOtherInfo);
        //etcontactType = (EditText)findViewById(R.id.etContactType);
        //categoryType = (EditText)findViewById(R.id.etCategory);
        etTitle = (EditText)findViewById(R.id.etTitle);
        region = (EditText)findViewById(R.id.etRegion);
        zipcode = (EditText)findViewById(R.id.etZipCode);
        etAddress2 = (EditText)findViewById(R.id.etAddress2);
        memo = (EditText)findViewById(R.id.etMemo);

        mCompositeDisposable = new CompositeDisposable();
        apiInterface = ApiUtils.getService();

        phoneNumber = "";
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
//            Log.d(TAG, "Phone number final: " + getPhone());
//            //phoneNumber = getPhone();
//            _mst = getPhone();
//            for (String op: _mst) {
//                Log.d("Device Information", String.valueOf(op));
//            }

//            try {
//                TelephonyManager telemamanger = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//                Log.d(TAG, "Serial: " + telemamanger.getSimSerialNumber());
//                Log.d(TAG, "Sim: " + telemamanger.getLine1Number());
//                phoneNumber = telemamanger.getLine1Number().replace("+", "");
//
//            }catch (Exception e){
//                phoneNumber = "";
//            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<SubscriptionInfo> subscription = new ArrayList<>();
                subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
                for (int i = 0; i < 1; i++) {
                    SubscriptionInfo info = subscription.get(i);
                    Log.d(TAG, "number from subscription " + info.getNumber());
                    if(info.getNumber().equals(null)){
                        phoneNumber = "";
                        devicePhone.setText("");
                    }else {
                        phoneNumber = info.getNumber();
                        devicePhone.setText(info.getNumber().replace("+",""));
                    }
                    Log.d(TAG, "network name : " + info.getCarrierName());
                    Log.d(TAG, "country iso " + info.getCountryIso());
                }
            }
        }catch (Exception e){
            Log.d(TAG, "Exception: "+e);
            phoneNumber = "";
            devicePhone.setText("");
        }
        getContactType();
        //testRead();
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_contact:
                if (checked){
                    contactListSelected = true;
                } else{
                    contactListSelected = false;
                }
                break;
            case R.id.checkbox_web:
                if (checked){
                    webSelected = true;
                } else{
                    webSelected = false;
                }
                break;
            // TODO: Veggie sandwich
        }
    }

    //--------------------- get phoneNumber methods ----------------------
//    private String getPhone() {
//        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
//            return "";
//        }
//        return phoneMgr.getLine1Number();
//    }

    @TargetApi(Build.VERSION_CODES.O)
    private ArrayList<String> getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        ArrayList<String> _lst =new ArrayList<>();
        _lst.add(String.valueOf(phoneMgr.getCallState()));
        _lst.add("IMEI NUMBER :-"+phoneMgr.getImei());
        _lst.add("MOBILE NUMBER :-"+phoneMgr.getLine1Number());
        _lst.add("SERIAL NUMBER :-"+phoneMgr.getSimSerialNumber());
        _lst.add("SIM OPERATOR NAME :-"+phoneMgr.getSimOperatorName());
        _lst.add("MEI NUMBER :-"+phoneMgr.getMeid());
        _lst.add("SIM STATE :-"+String.valueOf(phoneMgr.getSimState()));
        _lst.add("COUNTRY ISO :-"+phoneMgr.getSimCountryIso());
        return _lst;
    }

//    public int getIndex(String stateName){
//        int index = 0;
//        for(int i=0; i<stateList.size(); i++){
//            if(stateList.get(i).getSTATENAME().equals(stateName)){
//                index = i;
//                break;
//            }
//        }
//        return index;
//    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone());
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    //-------------------------------------------------------------------------------

    public void postReaderData(String sharedValue){
        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(ReadActivity.this, "", "Data Posting. Please wait.....", true);
            //String Data = "Firstname:Sohan|Lastname:Azad|Companyname:BeRich|Phone:8801819123123|Email:muhitunazad@gmail.com";
            String Data = "FirstName:"+name.getText().toString()+"|LastName:"+lastname.getText().toString()+"|CompanyName:"+company.getText().toString()+"|Phone:"+cell.getText().toString()+"|Email:"+email.getText().toString()+"|Address:"+address.getText().toString()+"|City:"+city.getText().toString()+"|State:"+state.getText().toString()+"|WorkPhoneExt:"+workNumberEnterprise.getText().toString()+"|Fax:"+faxNumber.getText().toString()+"|Website:"+websiteLink.getText().toString()+"|Category:"+category+"|TypeOfContact:"+contactType+"|RefPhone:"+realPhone.getText().toString()+"|Other:"+otherInfo.getText().toString()+"|WorkPhone:"+workNumber.getText().toString()+"|Title:"+etTitle.getText().toString()+"|Address1:"+etAddress2.getText().toString()+"|Region:"+region.getText().toString()+"|Zip:"+zipcode.getText().toString()+"|memo:"+memo.getText().toString();
            Log.d(TAG, "shared:"+sharedValue+" name:"+deviceOwnerName.getText().toString()+" fp:"+devicePhone.getText().toString()+" tp:"+cell.getText().toString()+" Data:"+Data);
            mCompositeDisposable.add(apiInterface.postReaderData(sharedValue, deviceOwnerName.getText().toString(), devicePhone.getText().toString(), cell.getText().toString(), Data) // while release give user id
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse, this::handleError));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    postReaderData(sharedValue);
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

    }


    private void handleResponse(ReaderDataPostedResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "Response of PF response "+clientResponse);

        if(clientResponse.getStatus()){
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(contactListSelected){
                        saveContacts();
                    }
                }
            });
            ad.setTitle("Status");
            ad.setMessage("Data posted successfully");
            ad.show();
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            ad.setTitle("Error");
            ad.setMessage(clientResponse.getMessage());
            ad.show();
        }

    }

    private void handleError(Throwable error) {
        dialog.dismiss();
        Log.d(TAG, "Error "+error);
        AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
        ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                postReaderData(sharedValue);
            }
        });
        ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        ad.setTitle("Error");
        ad.setMessage(error+"");
        ad.show();
        //Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }

    public void getContactType(){

        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(ReadActivity.this, "", "Data loading. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.getContactType() //userData.getUserDataModel().getEmName()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponsesContact, this::handleErrorsContact));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getContactType();
                }
            });
            ad.setMessage("Please check your internet connection and try again");
            ad.show();
        }
    }


    private void handleResponsesContact(ContactTypeResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "Response of product purchase view api "+clientResponse);

        if(clientResponse.getStatus()){
            Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            contactTypeList.clear();
            contactTypeList.addAll(clientResponse.getData());
            adapterTyp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
            adapterTyp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapterTyp.add("Select Contact Type");
            adapterTyp.addAll(contactTypeList);
            spinTyp.setAdapter(adapterTyp);
            getCategory();
        }else{
            //recyclerView.setAdapter(null);
            //Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // loadInvestorInfo();
                    getContactType();
                }
            });
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //finish();
                }
            });
            ad.setMessage("You are not allowed");
            ad.setCancelable(false);
            ad.show();
        }

    }

    private void handleErrorsContact(Throwable error) {
        dialog.dismiss();
        Log.d(TAG, "Error "+error);
        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }


    public void getCategory(){

        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(ReadActivity.this, "", "Data loading. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.getCategoryType() //userData.getUserDataModel().getEmName()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponsesCategory, this::handleErrorsCategory));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getCategory();
                }
            });
            ad.setMessage("Please check your internet connection and try again");
            ad.show();
        }
    }


    private void handleResponsesCategory(CategoryResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "Response of product purchase view api "+clientResponse);

        if(clientResponse.getStatus()){
            Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            categoryList.clear();
            categoryList.addAll(clientResponse.getData());
            adapterCat = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
            adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapterCat.add("Select Category");
            adapterCat.addAll(categoryList);
            spinCat.setAdapter(adapterCat);
        }else{
            //recyclerView.setAdapter(null);
            //Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // loadInvestorInfo();
                    getContactType();
                }
            });
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //finish();
                }
            });
            ad.setMessage("You are not allowed");
            ad.setCancelable(false);
            ad.show();
        }

    }

    private void handleErrorsCategory(Throwable error) {
        dialog.dismiss();
        Log.d(TAG, "Error "+error);
        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }

    public void getUser(){
        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(ReadActivity.this, "", "Data Loading. Please wait.....", true);
            //final APIInterface apiService = RetrofitClient.getClientMfg().create(APIInterface.class);
            //       Toast.makeText(getApplicationContext(), "Api "+OnlineOrder.someTest(MainActivity.headerValue), Toast.LENGTH_LONG).show();
            //Log.d(TAG, "name "+ InternalDataProvider.getInstance().getPortfolioManagerModel().getName()+" date1 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getFirstDate()+" date2 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getSecondDate()+" type "+InternalDataProvider.getInstance().getPortfolioManagerModel().getType());
            mCompositeDisposable.add(apiInterface.getProfile(cell.getText().toString()) // while release give user id
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseUser, this::handleErrorUser));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getUser();
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

    }


    private void handleResponseUser(ProfileResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "Response of PF response "+clientResponse);

        if(clientResponse.getStatus()){
            name.setText(clientResponse.getData().get(0).getFirstName());
            lastname.setText(clientResponse.getData().get(0).getLastName());
            company.setText(clientResponse.getData().get(0).getCompanyName());
            etTitle.setText(clientResponse.getData().get(0).getTitle());
            address.setText(clientResponse.getData().get(0).getAddress());
            etAddress2.setText(clientResponse.getData().get(0).getAddress1());
            city.setText(clientResponse.getData().get(0).getCity());

            //comClient.setText(clientResponse.getData().get(0).getState());
            state.setText(clientResponse.getData().get(0).getState());
            //s.setText(clientResponse.getData().get(0).getFirstName());

            email.setText(clientResponse.getData().get(0).getEmail());
            workNumber.setText(clientResponse.getData().get(0).getWorkPhone());
            workNumberEnterprise.setText(clientResponse.getData().get(0).getWorkPhoneExt());
            faxNumber.setText(clientResponse.getData().get(0).getFax());

            categoryType.setText(clientResponse.getData().get(0).getCategory());
            etcontactType.setText(clientResponse.getData().get(0).getTypeOfContact());

            websiteLink.setText(clientResponse.getData().get(0).getWebsite());
            realPhone.setText(clientResponse.getData().get(0).getRefPhone());
            otherInfo.setText(clientResponse.getData().get(0).getOther());

//            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
//            ad.setCancelable(false);
//            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    //startActivity(new Intent(getApplicationContext(), MainActivity.class));
//
//                }
//            });
//            ad.setTitle("Status");
//            ad.setMessage("Data retrieved successfully");
//            ad.show();
        }else{
//            AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
//            ad.setCancelable(false);
//            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                }
//            });
//            ad.setTitle("Error");
//            ad.setMessage(clientResponse.getMessage());
//            ad.show();
        }

    }

    private void handleErrorUser(Throwable error) {
        dialog.dismiss();
//        Log.d(TAG, "Error "+error);
//        AlertDialog.Builder ad = new AlertDialog.Builder(ReadActivity.this);
//        ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                getUser();
//            }
//        });
//        ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//            }
//        });
//        ad.setTitle("Error");
//        ad.setMessage(error+"");
//        ad.show();
        //Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }

    public void saveContacts(){
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, cell.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name.getText().toString());
        startActivity(intent);
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

    public void testRead(){
        String text = "{\"fn\":\"nTunvir Rahman test android write 2\",\"ln\":\"tusher\",\"pn\":\"01552746442\",\"ad1\":\"hhdbbey\",\"ct\":\"ydhjd\",\"st\":\"ak\",\"em\":\"tyeh@hjdj.com\",\"wn\":\"01552746442\",\"cn\":\"Jjdjd\",\"wne\":\"\",\"fx\":\"\",\"wl\":\"\",\"cat\":\"Category1\",\"cty\":\"Main\",\"rp\":\"\",\"o\":\"\",\"tl\":\"iOS dev latest try\",\"ad2\":\"Bdbhdh\",\"z\":\"1400\",\"rg\":\"Hhehhejd\"}";

        JsonObject convertedObject = new Gson().fromJson(text.trim(), JsonObject.class);

        name.setText(convertedObject.get("fn").toString().replaceAll("^\"|\"$", ""));
        lastname.setText(convertedObject.get("ln").toString().replaceAll("^\"|\"$", ""));
        cell.setText(convertedObject.get("pn").toString().replaceAll("^\"|\"$", ""));
        address.setText(convertedObject.get("ad1").toString().replaceAll("^\"|\"$", ""));
        city.setText(convertedObject.get("ct").toString().replaceAll("^\"|\"$", ""));
        state.setText(convertedObject.get("st").toString().replaceAll("^\"|\"$", ""));
        email.setText(convertedObject.get("em").toString().replaceAll("^\"|\"$", ""));
        workNumber.setText(convertedObject.get("wn").toString().replaceAll("^\"|\"$", ""));
        company.setText(convertedObject.get("cn").toString().replaceAll("^\"|\"$", ""));
        workNumberEnterprise.setText(convertedObject.get("wne").toString().replaceAll("^\"|\"$", ""));
        faxNumber.setText(convertedObject.get("fx").toString().replaceAll("^\"|\"$", ""));
        websiteLink.setText(convertedObject.get("wl").toString().replaceAll("^\"|\"$", ""));
        realPhone.setText(convertedObject.get("rp").toString().replaceAll("^\"|\"$", ""));
        otherInfo.setText(convertedObject.get("o").toString().replaceAll("^\"|\"$", ""));
        etcontactType.setText(convertedObject.get("cty").toString().replaceAll("^\"|\"$", ""));
        categoryType.setText(convertedObject.get("cat").toString().replaceAll("^\"|\"$", ""));
        etTitle.setText(convertedObject.get("tl").toString().replaceAll("^\"|\"$", ""));
        etAddress2.setText(convertedObject.get("ad2").toString().replaceAll("^\"|\"$", ""));
        region.setText(convertedObject.get("rg").toString().replaceAll("^\"|\"$", ""));
        zipcode.setText(convertedObject.get("z").toString().replaceAll("^\"|\"$", ""));
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }


//    private void buildTagViews(NdefMessage[] msgs) {
//        if (msgs == null || msgs.length == 0) return;
//
//        String text = "";
////        String tagId = new String(msgs[0].getRecords()[0].getType());
//        byte[] payload = msgs[0].getRecords()[0].getPayload();
//        Log.d(TAG, "payload "+payload);
//        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
//        Log.d(TAG, "Text encoding "+text);
//        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
//        Log.d(TAG, "language code length "+languageCodeLength);
//        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
//
//        try {
//            // Get the Text
//            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
//            Log.d(TAG, "Retrieved Text "+text);
//            Log.d(TAG, "payload "+payload+" language Code length "+languageCodeLength+" textEncoding "+textEncoding+" payload length "+payload.length);
//            //Toast.makeText(getApplicationContext(), text.toString(), Toast.LENGTH_LONG).show();
//        } catch (UnsupportedEncodingException e) {
//            Log.d(TAG, "UnsupportedEncoding"+ e.toString());
//        }
////        readTag.setText("NFC Content: " + text);
//        try {
//            //JSONObject jsonObject = new JSONObject(text.toString());  //"{\"phonetype\":\"N95\",\"cat\":\"WP\"}"
////            JsonParser jsonParser = new JsonParser();
////            JsonObject jo = (JsonObject)jsonParser.parse(text);
////            JsonObject jsonObject = (new JsonParser()).parse(text).getAsJsonObject();
////            Log.d(TAG, "Test "+jsonObject.get("FirstName"));
//
//            JsonObject convertedObject = new Gson().fromJson(text.trim().replace("\\u{02}en",""), JsonObject.class);
//
//            name.setText(convertedObject.get("fn").toString().replaceAll("^\"|\"$", ""));
//            lastname.setText(convertedObject.get("ln").toString().replaceAll("^\"|\"$", ""));
//            cell.setText(convertedObject.get("pn").toString().replaceAll("^\"|\"$", ""));
//            address.setText(convertedObject.get("ad1").toString().replaceAll("^\"|\"$", ""));
//            city.setText(convertedObject.get("ct").toString().replaceAll("^\"|\"$", ""));
//            state.setText(convertedObject.get("st").toString().replaceAll("^\"|\"$", ""));
//            email.setText(convertedObject.get("em").toString().replaceAll("^\"|\"$", ""));
//            workNumber.setText(convertedObject.get("wn").toString().replaceAll("^\"|\"$", ""));
//            company.setText(convertedObject.get("cn").toString().replaceAll("^\"|\"$", ""));
//            workNumberEnterprise.setText(convertedObject.get("wne").toString().replaceAll("^\"|\"$", ""));
//            faxNumber.setText(convertedObject.get("fx").toString().replaceAll("^\"|\"$", ""));
//            websiteLink.setText(convertedObject.get("wl").toString().replaceAll("^\"|\"$", ""));
//            realPhone.setText(convertedObject.get("rp").toString().replaceAll("^\"|\"$", ""));
//            otherInfo.setText(convertedObject.get("o").toString().replaceAll("^\"|\"$", ""));
//            etcontactType.setText(convertedObject.get("cty").toString().replaceAll("^\"|\"$", ""));
//            categoryType.setText(convertedObject.get("cat").toString().replaceAll("^\"|\"$", ""));
//            etTitle.setText(convertedObject.get("tl").toString().replaceAll("^\"|\"$", ""));
//            etAddress2.setText(convertedObject.get("ad2").toString().replaceAll("^\"|\"$", ""));
//            region.setText(convertedObject.get("rg").toString().replaceAll("^\"|\"$", ""));
//            zipcode.setText(convertedObject.get("z").toString().replaceAll("^\"|\"$", ""));
//
//
//
//
////            workNumberEnterprise = (EditText)findViewById(R.id.etWorkNumberEnterprise);
////            faxNumber = (EditText)findViewById(R.id.etFaxNumber);
////            websiteLink = (EditText)findViewById(R.id.etWebsiteLink);
////            realPhone = (EditText)findViewById(R.id.etRealPhone);
////            otherInfo = (EditText)findViewById(R.id.etOtherInfo);
////            etcontactType = (EditText)findViewById(R.id.etContactType);
////            categoryType = (EditText)findViewById(R.id.etCategory);
//        }catch (Exception e){
//            Log.d(TAG, "Exception in reading "+e.toString());
//        }
////        catch (JSONException err){
////            Log.d("Error", err.toString());
////        }
////        catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
////        }
//    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        try {
            text = new String(payload, "UTF-8");
            JsonObject convertedObject = new Gson().fromJson(text.trim().replace("en{\"a\":","{\"a\":"), JsonObject.class);

//            name.setText(convertedObject.get("fn").toString().replaceAll("^\"|\"$", ""));
//            lastname.setText(convertedObject.get("ln").toString().replaceAll("^\"|\"$", ""));
//            cell.setText(convertedObject.get("pn").toString().replaceAll("^\"|\"$", ""));
//            address.setText(convertedObject.get("ad1").toString().replaceAll("^\"|\"$", ""));
//            city.setText(convertedObject.get("ct").toString().replaceAll("^\"|\"$", ""));
//            state.setText(convertedObject.get("st").toString().replaceAll("^\"|\"$", ""));
//            email.setText(convertedObject.get("em").toString().replaceAll("^\"|\"$", ""));
//            workNumber.setText(convertedObject.get("wn").toString().replaceAll("^\"|\"$", ""));
//            company.setText(convertedObject.get("cn").toString().replaceAll("^\"|\"$", ""));
//            workNumberEnterprise.setText(convertedObject.get("wne").toString().replaceAll("^\"|\"$", ""));
//            faxNumber.setText(convertedObject.get("fx").toString().replaceAll("^\"|\"$", ""));
//            websiteLink.setText(convertedObject.get("wl").toString().replaceAll("^\"|\"$", ""));
//            categoryType.setText(convertedObject.get("cat").toString().replaceAll("^\"|\"$", ""));
//            etcontactType.setText(convertedObject.get("cty").toString().replaceAll("^\"|\"$", ""));
//            realPhone.setText(convertedObject.get("rp").toString().replaceAll("^\"|\"$", ""));
//            otherInfo.setText(convertedObject.get("o").toString().replaceAll("^\"|\"$", ""));
//            etTitle.setText(convertedObject.get("tl").toString().replaceAll("^\"|\"$", ""));
//            etAddress2.setText(convertedObject.get("ad2").toString().replaceAll("^\"|\"$", ""));
//            zipcode.setText(convertedObject.get("z").toString().replaceAll("^\"|\"$", ""));
//            region.setText(convertedObject.get("rg").toString().replaceAll("^\"|\"$", ""));

            name.setText(convertedObject.get("a").toString().replaceAll("^\"|\"$", ""));
            lastname.setText(convertedObject.get("b").toString().replaceAll("^\"|\"$", ""));
            cell.setText(convertedObject.get("c").toString().replaceAll("^\"|\"$", ""));
            address.setText(convertedObject.get("d").toString().replaceAll("^\"|\"$", ""));
            city.setText(convertedObject.get("e").toString().replaceAll("^\"|\"$", ""));
            state.setText(convertedObject.get("f").toString().replaceAll("^\"|\"$", ""));
            email.setText(convertedObject.get("g").toString().replaceAll("^\"|\"$", ""));
            workNumber.setText(convertedObject.get("h").toString().replaceAll("^\"|\"$", ""));
            company.setText(convertedObject.get("i").toString().replaceAll("^\"|\"$", ""));
            workNumberEnterprise.setText(convertedObject.get("j").toString().replaceAll("^\"|\"$", ""));
            faxNumber.setText(convertedObject.get("k").toString().replaceAll("^\"|\"$", ""));
            websiteLink.setText(convertedObject.get("l").toString().replaceAll("^\"|\"$", ""));
//            categoryType.setText(convertedObject.get("m").toString().replaceAll("^\"|\"$", ""));
//            etcontactType.setText(convertedObject.get("n").toString().replaceAll("^\"|\"$", ""));
            realPhone.setText(convertedObject.get("o").toString().replaceAll("^\"|\"$", ""));
            otherInfo.setText(convertedObject.get("p").toString().replaceAll("^\"|\"$", ""));
            etTitle.setText(convertedObject.get("q").toString().replaceAll("^\"|\"$", ""));
            etAddress2.setText(convertedObject.get("r").toString().replaceAll("^\"|\"$", ""));
            zipcode.setText(convertedObject.get("s").toString().replaceAll("^\"|\"$", ""));
            region.setText(convertedObject.get("t").toString().replaceAll("^\"|\"$", ""));

        }catch (Exception e){
            Log.d(TAG, "Exception in reading "+e.toString());
        }

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        switch (parent.getId()) {
            case R.id.spinCategory:
                if(pos==0){
                    category = "";
                }else {
                    category = categoryList.get(pos-1).getDescription();
                }
                //Toast.makeText(getApplicationContext(), category, Toast.LENGTH_LONG).show();
                break;
            case R.id.spinContactType:
                if(pos==0){
                    contactType = "";
                }else {
                    contactType = contactTypeList.get(pos-1).getDescription();
                }
                //Toast.makeText(getApplicationContext(), contactType, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
//
    private String validate() {
        //boolean isValid = true;

        String isValid = null;
//
        if (TextUtils.isEmpty(name.getText().toString().trim())) {
            isValid = "Please enter first name";
            return isValid;
        }
        if (TextUtils.isEmpty(cell.getText().toString().trim())) {
            isValid = "Please enter cell number";
            return isValid;
        }
        if (TextUtils.isEmpty(lastname.getText().toString().trim())) {
            isValid = "Please enter last name";
            return isValid;
        }
//        if (TextUtils.isEmpty(zipcode.getText().toString().trim())) {
//            isValid = "Please enter zip code";
//            return isValid;
//        }
//        if (TextUtils.isEmpty(address.getText().toString().trim())) {
//            isValid = "Please enter company name";
//            return isValid;
//        }
//        if (TextUtils.isEmpty(city.getText().toString().trim())) {
//            isValid = "Please enter city";
//            return isValid;
//        }
//        if (TextUtils.isEmpty(state.getText().toString().trim())) {
//            isValid = "Please enter state";
//            return isValid;
//        }
        if (TextUtils.isEmpty(email.getText().toString().trim())) {
            isValid = "Please enter email";
            return isValid;
        }
        if (TextUtils.isEmpty(workNumber.getText().toString().trim())) {
            isValid = "Please enter work number";
            return isValid;
        }
        if(TextUtils.isEmpty(workNumberEnterprise.getText().toString().trim())){

        }else{
            if(workNumberEnterprise.getText().toString().length()>1 & workNumberEnterprise.getText().toString().length()<10){

            }else{
                isValid = "Work number extension should not exceed 10 digit";
                return isValid;
            }
        }
        if (TextUtils.isEmpty(devicePhone.getText().toString().trim())) {
            isValid = "Please enter device phone number";
            return isValid;
        }
        if (TextUtils.isEmpty(deviceOwnerName.getText().toString().trim())) {
            isValid = "Please enter device owner name";
            return isValid;
        }
        return isValid;
    }
//
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, nfcAdapter);


    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

//    /**
//     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
//     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
//     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

}
