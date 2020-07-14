package com.service.sohan.nfcreadwritetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.service.sohan.nfcreadwritetest.Api.ApiInterface;
import com.service.sohan.nfcreadwritetest.Api.ApiUtils;
import com.service.sohan.nfcreadwritetest.Model.CategoryResponse;
import com.service.sohan.nfcreadwritetest.Model.ContactTypeResponse;
import com.service.sohan.nfcreadwritetest.Model.ProfileResponse;
import com.service.sohan.nfcreadwritetest.Model.StateResponse;
import com.service.sohan.nfcreadwritetest.Model.WriterPostedResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WriteActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    //public static final String TAG = "NfcDemo";
    private final String TAG = "nfc_" + this.getClass().getSimpleName();
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView readTag, tvVersion;
    Button btnExit, btnWrite, btnClear;
    EditText name, cell, lastname, address, city, state, email, workNumber, company, workNumberEnterprise, faxNumber, websiteLink, realPhone, otherInfo, etTitle, etAddress2, region, zipcode;
    private CompositeDisposable mCompositeDisposable;
    ApiInterface apiInterface;
    ProgressDialog dialog;
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
    int totalThreshold = 0;
    int finalThreshold = 320; //376 previously, without input tag size is 164, latest 336 now 280, now 303, latest 320 before 303

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        context = this;
        tvVersion = (TextView)findViewById(R.id.tvVersion);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        tvVersion.setText("Version: "+versionName);

        readFromIntent(getIntent());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            //
            finish();
        }

        logo = (ImageView)findViewById(R.id.imgLogo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
            }
        });

        stateList = new ArrayList<>();

        llMain = (LinearLayout)findViewById(R.id.llMain);
        btnExit = (Button)findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
//                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                homeIntent.addCategory( Intent.CATEGORY_HOME );
//                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(homeIntent);

            }
        });
        categoryList = new ArrayList<>();
        contactTypeList = new ArrayList<>();

//        categoryList.add("Category 1");
//        categoryList.add("Category 2");
//
//        typeList.add("Mobile");
//        typeList.add("Work");
//        typeList.add("Home");
//        typeList.add("Main");
//        typeList.add("Work Fax");
//        typeList.add("Home Fax");
//        typeList.add("Other");

//        spinCat = (Spinner)findViewById(R.id.spinCategory);
//        spinCat.setOnItemSelectedListener(this);
//        adapterCat = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
//        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterCat.add("Select Category");
//        adapterCat.addAll(categoryList);
//        spinCat.setAdapter(adapterCat);
//
//        spinTyp = (Spinner)findViewById(R.id.spinContactType);
//        spinTyp.setOnItemSelectedListener(this);
//        adapterTyp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
//        adapterTyp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterTyp.add("Select Contact Type");
//        adapterTyp.addAll(contactTypeList);
//        spinTyp.setAdapter(adapterTyp);

        btnWrite = (Button)findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // previous working -------------------------------
                if(validate()==null){
                   // Toast.makeText(getApplicationContext(), "All OK", Toast.LENGTH_LONG).show();
                    try {
                        if(myTag == null) {
                            Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                        } else {
                            totalThreshold = name.getText().toString().getBytes().length+lastname.getText().toString().getBytes().length+cell.getText().toString().getBytes().length+address.getText().toString().getBytes().length+city.getText().toString().getBytes().length+stateValue.toString().getBytes().length+email.getText().toString().getBytes().length+workNumber.getText().toString().getBytes().length+company.getText().toString().getBytes().length+workNumberEnterprise.getText().toString().getBytes().length+faxNumber.getText().toString().getBytes().length+websiteLink.getText().toString().getBytes().length+realPhone.getText().toString().getBytes().length+otherInfo.getText().toString().getBytes().length+etTitle.getText().toString().getBytes().length+etAddress2.getText().toString().getBytes().length+zipcode.getText().toString().getBytes().length+region.getText().toString().getBytes().length;
                            Log.d(TAG, "Total threshold "+totalThreshold);
                            if(totalThreshold < finalThreshold) {
                               //String messageData = "{\"fn\":" + "\"" + name.getText().toString().trim() + "\"" + ",\"ln\":" + "\"" + lastname.getText().toString() + "\"" + ",\"pn\":" + "\"" + cell.getText().toString() + "\"" + ",\"ad1\":" + "\"" + address.getText().toString() + "\"" + ",\"ct\":" + "\"" + city.getText().toString() + "\"" + ",\"st\":" + "\"" + stateValue + "\"" + ",\"em\":" + "\"" + email.getText().toString() + "\"" + ",\"wn\":" + "\"" + workNumber.getText().toString() + "\"" + ",\"cn\":" + "\"" + company.getText().toString() + "\"" + ",\"wne\":" + "\"" + workNumberEnterprise.getText().toString() + "\"" + ",\"fx\":" + "\"" + faxNumber.getText().toString() + "\"" + ",\"wl\":" + "\"" + websiteLink.getText().toString() + "\"" + ",\"cat\":" + "\"" + category + "\"" + ",\"cty\":" + "\"" + contactType + "\"" + ",\"rp\":" + "\"" + realPhone.getText().toString() + "\"" + ",\"o\":" + "\"" + otherInfo.getText().toString() + "\"" + ",\"tl\":" + "\"" + etTitle.getText().toString() + "\"" + ",\"ad2\":" + "\"" + etAddress2.getText().toString() + "\"" + ",\"z\":" + "\"" + zipcode.getText().toString() + "\"" + ",\"rg\":" + "\"" + region.getText().toString() + "\"" + "}";
                               //String messageData = "{\"a\":" + "\"" + name.getText().toString().trim() + "\"" + ",\"b\":" + "\"" + lastname.getText().toString() + "\"" + ",\"c\":" + "\"" + cell.getText().toString() + "\"" + ",\"d\":" + "\"" + address.getText().toString() + "\"" + ",\"e\":" + "\"" + city.getText().toString() + "\"" + ",\"f\":" + "\"" + stateValue + "\"" + ",\"g\":" + "\"" + email.getText().toString() + "\"" + ",\"h\":" + "\"" + workNumber.getText().toString() + "\"" + ",\"i\":" + "\"" + company.getText().toString() + "\"" + ",\"j\":" + "\"" + workNumberEnterprise.getText().toString() + "\"" + ",\"k\":" + "\"" + faxNumber.getText().toString() + "\"" + ",\"l\":" + "\"" + websiteLink.getText().toString() + "\"" + ",\"m\":" + "\"" + category + "\"" + ",\"n\":" + "\"" + contactType + "\"" + ",\"o\":" + "\"" + realPhone.getText().toString() + "\"" + ",\"p\":" + "\"" + otherInfo.getText().toString() + "\"" + ",\"q\":" + "\"" + etTitle.getText().toString() + "\"" + ",\"r\":" + "\"" + etAddress2.getText().toString() + "\"" + ",\"s\":" + "\"" + zipcode.getText().toString() + "\"" + ",\"t\":" + "\"" + region.getText().toString() + "\"" + "}";
                               String messageData = "{\"a\":" + "\"" + name.getText().toString().trim() + "\"" + ",\"b\":" + "\"" + lastname.getText().toString() + "\"" + ",\"c\":" + "\"" + cell.getText().toString() + "\"" + ",\"d\":" + "\"" + address.getText().toString() + "\"" + ",\"e\":" + "\"" + city.getText().toString() + "\"" + ",\"f\":" + "\"" + stateValue + "\"" + ",\"g\":" + "\"" + email.getText().toString() + "\"" + ",\"h\":" + "\"" + workNumber.getText().toString() + "\"" + ",\"i\":" + "\"" + company.getText().toString() + "\"" + ",\"j\":" + "\"" + workNumberEnterprise.getText().toString() + "\"" + ",\"k\":" + "\"" + faxNumber.getText().toString() + "\"" + ",\"l\":" + "\"" + websiteLink.getText().toString() + "\"" + ",\"o\":" + "\"" + realPhone.getText().toString() + "\"" + ",\"p\":" + "\"" + otherInfo.getText().toString() + "\"" + ",\"q\":" + "\"" + etTitle.getText().toString() + "\"" + ",\"r\":" + "\"" + etAddress2.getText().toString() + "\"" + ",\"s\":" + "\"" + zipcode.getText().toString() + "\"" + ",\"t\":" + "\"" + region.getText().toString() + "\"" + "}";

                               //String messageData = "{\"fn\":" + "\"" + name + "\"" + ",\"ln\":" + "\"" + lastName + "\"" + ",\"pn\":" + "\"" + cell + "\"" + ",\"ad1\":" + "\"" + address1 + "\"" + ",\"ct\":" + "\"" + city + ",\"st\":" + "\"" + state + ",\"em\":" + "\"" + email + ",\"wn\":" + "\"" + workNumber + ",\"cn\":\(company),\"wne\":\(workNumberExt),\"fx\":\(fax),\"wl\":\(website),\"cat\":\(categoryType),\"cty\":\(contactType),\"rp\":\(refPhone),\"o\":\(otherInf),\"tl\":\(title),\"ad2\":\(address2),\"z\":\(zip),\"rg\":\(region)}"
                               //String test =  "{\"fn\":" + "\"" + name + "\"" + "}";
                              // String withoutInputOld = "{\"fn\":" + "\"" + "\"" + ",\"ln\":" + "\"" + "\"" + ",\"pn\":" + "\"" +  "\"" + ",\"ad1\":" + "\"" +  "\"" + ",\"ct\":" + "\"" +  "\"" + ",\"st\":" + "\"" +  "\"" + ",\"em\":" + "\"" +  "\"" + ",\"wn\":" + "\"" +  "\"" + ",\"cn\":" + "\"" +  "\"" + ",\"wne\":" + "\"" + "\"" + ",\"fx\":" + "\"" +  "\"" + ",\"wl\":" + "\"" + "\"" + ",\"cat\":" + "\"" + "\"" + ",\"cty\":" + "\"" + "\"" + ",\"rp\":" + "\"" + "\"" + ",\"o\":" + "\"" + "\"" + ",\"tl\":" + "\"" + "\"" + ",\"ad2\":" + "\"" + "\"" + ",\"z\":" + "\"" + "\"" + ",\"rg\":" + "\"" + "\"" + "}";
                               //String withoutInputNew = "{\"a\":" + "\"" + "\"" + ",\"b\":" + "\"" + "\"" + ",\"c\":" + "\"" +  "\"" + ",\"d\":" + "\"" +  "\"" + ",\"e\":" + "\"" +  "\"" + ",\"f\":" + "\"" +  "\"" + ",\"g\":" + "\"" +  "\"" + ",\"h\":" + "\"" +  "\"" + ",\"i\":" + "\"" +  "\"" + ",\"j\":" + "\"" + "\"" + ",\"k\":" + "\"" +  "\"" + ",\"l\":" + "\"" + "\"" + ",\"o\":" + "\"" + "\"" + ",\"p\":" + "\"" + "\"" + ",\"q\":" + "\"" + "\"" + ",\"r\":" + "\"" + "\"" + ",\"s\":" + "\"" + "\"" + ",\"t\":" + "\"" + "\"" + "}";
                               //String withoutInputNew1 = "{\"a\":" + "\"" + "\"" + ",\"b\":" + "\"" + "\"" + ",\"c\":" + "\"" +  "\"" + ",\"d\":" + "\"" +  "\"" + ",\"e\":" + "\"" +  "\"" + ",\"f\":" + "\"" +  "\"" + ",\"g\":" + "\"" +  "\"" + ",\"h\":" + "\"" +  "\"" + ",\"i\":" + "\"" +  "\"" + ",\"j\":" + "\"" + "\"" + ",\"k\":" + "\"" +  "\"" + ",\"l\":" + "\"" + "\"" + ",\"m\":" + "\"" + "\"" + ",\"n\":" + "\"" + "\"" + ",\"o\":" + "\"" + "\"" + ",\"p\":" + "\"" + "\"" + ",\"q\":" + "\"" + "\"" + ",\"r\":" + "\"" + "\"" + ",\"s\":" + "\"" + "\"" + ",\"t\":" + "\"" + "\"" + "}";
//
//                                String testMessage = "{\"fn\":\"nTunvir Rahman test ios write 2\",\"ln\":\"tusher\",\"pn\":\"01552746442\",\"ad1\":\"hhdbbey\",\"ct\":\"ydhjd\",\"st\":\"\",\"em\":\"tyeh@hjdj.com\",\"wn\":\"01552746442\",\"cn\":\"Jjdjd\",\"wne\":\"\",\"fx\":\"\",\"wl\":\"\",\"cat\":\"Category1\",\"cty\":\"Main\",\"rp\":\"\",\"o\":\"\",\"tl\":\"iOS dev latest try\",\"ad2\":\"Bdbhdh\",\"z\":\"1400\",\"rg\":\"Hhehhejd\"}";
                                //Log.d(TAG, "Without input length old "+withoutInputOld.getBytes().length);
                                //Log.d(TAG, "Without input length new "+withoutInputNew.getBytes().length);
                                //Log.d(TAG, "Without input length new1 "+withoutInputNew1.getBytes().length);
//                                Log.d(TAG, "full message length "+messageData.getBytes().length);
//                                Log.d(TAG, "message gata "+messageData);

                                write(messageData, myTag);


                                //String testMessage = "{\"fn\":\"nTunvir Rahman test ios write 2\",\"ln\":\"tusher\",\"pn\":\"01552746442\",\"ad1\":\"hhdbbey\",\"ct\":\"ydhjd\",\"st\":\"\",\"em\":\"tyeh@hjdj.com\",\"wn\":\"01552746442\",\"cn\":\"Jjdjd\",\"wne\":\"\",\"fx\":\"\",\"wl\":\"\",\"cat\":\"Category1\",\"cty\":\"Main\",\"rp\":\"\",\"o\":\"\",\"tl\":\"iOS dev latest try\",\"ad2\":\"Bdbhdh\",\"z\":\"1400\",\"rg\":\"Hhehhejd\"}";
                                //String testMessage = "{\"fn\":\"nTunvir Rahman ios\",\"ln\":\"tusher\",\"pn\":\"01552746442\",\"ad1\":\"hhdbbey\",\"ct\":\"ydhjd\",\"st\":\"\",\"em\":\"tyeh@hjdj.com\",\"wn\":\"01552746442\",\"cn\":\"Jjdjd\",\"wne\":\"\",\"fx\":\"\",\"wl\":\"\",\"cat\":\"Category1\",\"cty\":\"Main\",\"rp\":\"\",\"o\":\"\",\"tl\":\"iOS dev latest try\",\"ad2\":\"Bdbhdh\",\"z\":\"1400\",\"rg\":\"Hhehhejd\"}";
//                                Log.d(TAG, "Without input length "+testMessage.getBytes().length);
//                                Log.d(TAG, "full message length "+testMessage.getBytes().length);
//                                Log.d(TAG, "message gata "+testMessage);
//                                write(testMessage, myTag);

                                Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG).show();
                                postWriterData();
                                //Toast.makeText(context, "All ok", Toast.LENGTH_LONG).show();
                            }else{
                                AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
                                ad.setCancelable(false);
                                ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                ad.setTitle("Error");
                                ad.setMessage("Cannot write more than 320 bytes, please reduce some data");
                                ad.show();
                            }
                        }
                    }
                    catch (Exception e){
                        AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
                        ad.setCancelable(false);
                        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        ad.setTitle("Error");
                        ad.setMessage("Error in data");
                        ad.show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), validate(), Toast.LENGTH_LONG).show();
                }

            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;
            //finish();
        }

        comClient = (AutoCompleteTextView)findViewById(R.id.comSymbol);
        comClient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (comClient.getText().length() > 0) {

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(llMain.getWindowToken(), 0);

                    //Toast.makeText(getApplicationContext(), "selected "+comClient.getText().toString(), Toast.LENGTH_LONG).show();
//                    String b4companyCode = comClient.getText().toString();
//                    String[] parts = b4companyCode.split("-");
//                    String code = parts[0]; // 004
//                    companyCode = code;

                    String Code = comClient.getText().toString();
                    companyCode = Code;
                    int index = getIndex(companyCode);
                    stateValue = stateList.get(index).getState();
                    //Toast.makeText(getApplicationContext(), "selected state "+stateValue, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a company", Toast.LENGTH_LONG).show();
                }
            }
        });
        sourceAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);

        comClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comClient.showDropDown();
            }
        });

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        btnClear = (Button)findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name.setText("");
                lastname.setText("");
                cell.setText("");
                address.setText("");
                city.setText("");
                state.setText("");
                email.setText("");
                workNumber.setText("");
                company.setText("");
                workNumberEnterprise.setText("");
                faxNumber.setText("");
                websiteLink.setText("");
                realPhone.setText("");
                otherInfo.setText("");
                etTitle.setText("");
                etAddress2.setText("");
                zipcode.setText("");
                region.setText("");
            }
        });

        name = (EditText)findViewById(R.id.etFirstName);
        lastname = (EditText)findViewById(R.id.etLastName);
        cell = (EditText)findViewById(R.id.etCell);
        cell.setOnFocusChangeListener(this);
        cell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    //Toast.makeText(getApplicationContext(), "Got the focus", Toast.LENGTH_LONG).show();
                } else {
                   // Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
                    getUser();
                }
            }
        });

        address = (EditText)findViewById(R.id.etAddress);
        region = (EditText)findViewById(R.id.etRegion);
        zipcode = (EditText)findViewById(R.id.etZipCode);
        city = (EditText)findViewById(R.id.etCity);
        state = (EditText)findViewById(R.id.etState);
        email = (EditText)findViewById(R.id.etEmail);
        workNumber = (EditText)findViewById(R.id.etWorkNumber);
        company = (EditText)findViewById(R.id.etCompany);
        workNumberEnterprise = (EditText)findViewById(R.id.etWorkNumberEnterprise);
        faxNumber = (EditText)findViewById(R.id.etFaxNumber);
        websiteLink = (EditText)findViewById(R.id.etWebsiteLink);
        realPhone = (EditText)findViewById(R.id.etRealPhone);
        otherInfo = (EditText)findViewById(R.id.etOtherInfo);
        etTitle = (EditText)findViewById(R.id.etTitle);
        etAddress2 = (EditText)findViewById(R.id.etAddress2);

        mCompositeDisposable = new CompositeDisposable();
        apiInterface = ApiUtils.getService();
        getStates();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String cellNumber = cell.getText().toString();

        if(cellNumber.length() == 0) {
            if(cell.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                //Toast.makeText(WriteActivity.this, "Dieser Liganame ist bereits vergeben", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public int getIndex(String stateName){
        int index = 0;
        for(int i=0; i<stateList.size(); i++){
            if(stateList.get(i).getSTATENAME().equals(stateName)){
                index = i;
                break;
            }
        }
        return index;
    }

//    public void saveContacts(){
//        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
//        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
//        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.getText().toString());
//        intent.putExtra(ContactsContract.Intents.Insert.PHONE, cell.getText().toString());
//        intent.putExtra(ContactsContract.Intents.Insert.NAME, name.getText().toString());
//        startActivity(intent);
//    }

    public void postWriterData(){
        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(WriteActivity.this, "", "Data Posting. Please wait.....", true);
            //final APIInterface apiService = RetrofitClient.getClientMfg().create(APIInterface.class);
            //       Toast.makeText(getApplicationContext(), "Api "+OnlineOrder.someTest(MainActivity.headerValue), Toast.LENGTH_LONG).show();
            //Log.d(TAG, "name "+ InternalDataProvider.getInstance().getPortfolioManagerModel().getName()+" date1 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getFirstDate()+" date2 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getSecondDate()+" type "+InternalDataProvider.getInstance().getPortfolioManagerModel().getType());
            String Data = "FirstName:"+name.getText().toString()+"|LastName:"+lastname.getText().toString()+"|CompanyName:"+company.getText().toString()+"|Phone:"+cell.getText().toString()+"|Email:"+email.getText().toString()+"|Address:"+address.getText().toString()+"|City:"+city.getText().toString()+"|State:"+stateValue+"|WorkPhoneExt:"+workNumberEnterprise.getText().toString()+"|Fax:"+faxNumber.getText().toString()+"|Website:"+websiteLink.getText().toString()+"|Category:"+category+"|TypeOfContact:"+contactType+"|RefPhone:"+realPhone.getText().toString()+"|Other:"+otherInfo.getText().toString()+"|WorkPhone:"+workNumber.getText().toString()+"|Title:"+etTitle.getText().toString()+"|Address1:"+etAddress2.getText().toString()+"|Zip:"+zipcode.getText().toString()+"|Region:"+region.getText().toString();

            mCompositeDisposable.add(apiInterface.postWriteData(cell.getText().toString(), email.getText().toString(), Data) // while release give user id
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse, this::handleError));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    postWriterData();
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


    private void handleResponse(WriterPostedResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "Response of PF response "+clientResponse);

        if(clientResponse.getStatus()){
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
            ad.setCancelable(false);
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            });
            ad.setTitle("Status");
            ad.setMessage("Data written to nfg tag and posted successfully on web");
            ad.show();
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
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
        AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
        ad.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                postWriterData();
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

    public void getStates(){

        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(WriteActivity.this, "", "Data loading. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.getStates() //userData.getUserDataModel().getEmName()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponses, this::handleErrors));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getStates();
                }
            });
            ad.setMessage("Please check your internet connection and try again");
            ad.show();
        }
    }


    private void handleResponses(StateResponse clientResponse) {
        dialog.dismiss();

        Log.d(TAG, "State Response "+clientResponse);

        if(clientResponse.getStatus()){
            Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            stateList.clear();
            stateList.addAll(clientResponse.getData());
            sourceAdapter.clear();
            for (int i = 0; i < clientResponse.getData().size(); i++) {
                sourceAdapter.add(clientResponse.getData().get(i).getSTATENAME());
                //sourceAdapter.add(clientResponse.getData().get(i).getInvid()+"- "+clientResponse.getData().get(i).getInvname());
            }
            comClient.setAdapter(sourceAdapter);
            sourceAdapter.notifyDataSetChanged();
            comClient.setText("");
            //getContactType();
        }else{
            sourceAdapter.clear();
            //recyclerView.setAdapter(null);
            //Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ad.setMessage("You are not allowed");
            ad.setCancelable(false);
            ad.show();
        }

    }

    private void handleErrors(Throwable error) {
        dialog.dismiss();
        Log.d(TAG, "Error "+error);
        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }

//    public void getContactType(){
//
//        if(isNetworkAvailable()){
//            dialog = ProgressDialog.show(WriteActivity.this, "", "Data loading. Please wait.....", true);
//            mCompositeDisposable.add(apiInterface.getContactType() //userData.getUserDataModel().getEmName()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(this::handleResponsesContact, this::handleErrorsContact));
//        }else{
//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
//            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    finish();
//                }
//            });
//            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    getContactType();
//                }
//            });
//            ad.setMessage("Please check your internet connection and try again");
//            ad.show();
//        }
//    }
//
//
//    private void handleResponsesContact(ContactTypeResponse clientResponse) {
//        dialog.dismiss();
//
//        Log.d(TAG, "Response of product purchase view api "+clientResponse);
//
//        if(clientResponse.getStatus()){
//            Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
//            contactTypeList.clear();
//            contactTypeList.addAll(clientResponse.getData());
//            adapterTyp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
//            adapterTyp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            adapterTyp.add("Select Contact Type");
//            adapterTyp.addAll(contactTypeList);
//            spinTyp.setAdapter(adapterTyp);
//            //getCategory();
//        }else{
//            //recyclerView.setAdapter(null);
//            //Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
//            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // loadInvestorInfo();
//                    getContactType();
//                }
//            });
//            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    //finish();
//                }
//            });
//            ad.setMessage("You are not allowed");
//            ad.setCancelable(false);
//            ad.show();
//        }
//
//    }
//
//    private void handleErrorsContact(Throwable error) {
//        dialog.dismiss();
//        Log.d(TAG, "Error "+error);
//        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
//    }


//    public void getCategory(){
//
//        if(isNetworkAvailable()){
//            dialog = ProgressDialog.show(WriteActivity.this, "", "Data loading. Please wait.....", true);
//            mCompositeDisposable.add(apiInterface.getCategoryType() //userData.getUserDataModel().getEmName()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(this::handleResponsesCategory, this::handleErrorsCategory));
//        }else{
//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
//            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    finish();
//                }
//            });
//            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    getCategory();
//                }
//            });
//            ad.setMessage("Please check your internet connection and try again");
//            ad.show();
//        }
//    }
//
//
//    private void handleResponsesCategory(CategoryResponse clientResponse) {
//        dialog.dismiss();
//
//        Log.d(TAG, "Response of product purchase view api "+clientResponse);
//
//        if(clientResponse.getStatus()){
//            Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
//            categoryList.clear();
//            categoryList.addAll(clientResponse.getData());
//            adapterCat = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item);
//            adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            adapterCat.add("Select Category");
//            adapterCat.addAll(categoryList);
//            spinCat.setAdapter(adapterCat);
//        }else{
//            //recyclerView.setAdapter(null);
//            //Toast.makeText(getApplicationContext(), clientResponse.getMessage(), Toast.LENGTH_LONG).show();
//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
//            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // loadInvestorInfo();
//                    getContactType();
//                }
//            });
//            ad.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    //finish();
//                }
//            });
//            ad.setMessage("You are not allowed");
//            ad.setCancelable(false);
//            ad.show();
//        }
//
//    }
//
//    private void handleErrorsCategory(Throwable error) {
//        dialog.dismiss();
//        Log.d(TAG, "Error "+error);
//        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
//    }

    public void getUser(){
        if(isNetworkAvailable()){
            //dialog = ProgressDialog.show(WriteActivity.this, "", "Data Loading. Please wait.....", true);
            //final APIInterface apiService = RetrofitClient.getClientMfg().create(APIInterface.class);
            //       Toast.makeText(getApplicationContext(), "Api "+OnlineOrder.someTest(MainActivity.headerValue), Toast.LENGTH_LONG).show();
            //Log.d(TAG, "name "+ InternalDataProvider.getInstance().getPortfolioManagerModel().getName()+" date1 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getFirstDate()+" date2 "+InternalDataProvider.getInstance().getPortfolioManagerModel().getSecondDate()+" type "+InternalDataProvider.getInstance().getPortfolioManagerModel().getType());
            mCompositeDisposable.add(apiInterface.getProfile(cell.getText().toString()) // while release give user id
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseUser, this::handleErrorUser));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
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
        //dialog.dismiss();

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
            String stateValueString = clientResponse.getData().get(0).getState();
            //String stateValueString = "NY";
            if (stateValueString != null) {
                for(int i=0; i<stateList.size(); i++){
                    if(stateList.get(i).getState().equalsIgnoreCase(stateValueString)){
                        comClient.setText(stateList.get(i).getSTATENAME());
                        break;
                    }
                }
            }
            stateValue = clientResponse.getData().get(0).getState();
            //s.setText(clientResponse.getData().get(0).getFirstName());

            email.setText(clientResponse.getData().get(0).getEmail());
            workNumber.setText(clientResponse.getData().get(0).getWorkPhone());
            workNumberEnterprise.setText(clientResponse.getData().get(0).getWorkPhoneExt());
            faxNumber.setText(clientResponse.getData().get(0).getFax());
            region.setText(clientResponse.getData().get(0).getRegion());
            zipcode.setText(clientResponse.getData().get(0).getZip());

//            String comparedValue = clientResponse.getData().get(0).getCategory();
//            if (comparedValue != null) {
//                int position = 0;
//                for(int i=0; i<categoryList.size(); i++){
//                    if(categoryList.get(i).toString().equals(comparedValue)){
//                        position = i;
//                        break;
//                    }
//                }
//                spinCat.setSelection(position+1);
//            }
//            category = clientResponse.getData().get(0).getCategory();
//
//            String comparedValueCat = clientResponse.getData().get(0).getTypeOfContact();
//            if (comparedValueCat != null) {
////                int spinnerPosition = adapterCat.getPosition(comparedValue);
////                spinCat.setSelection(spinnerPosition);
//                int position = 0;
//                for(int i=0; i<contactTypeList.size(); i++){
//                    if(contactTypeList.get(i).toString().equals(comparedValueCat)){
//                        position = i;
//                        break;
//                    }
//                }
//                spinTyp.setSelection(position+1);
//            }
//            contactType = clientResponse.getData().get(0).getTypeOfContact();

            websiteLink.setText(clientResponse.getData().get(0).getWebsite());
            realPhone.setText(clientResponse.getData().get(0).getRefPhone());
            otherInfo.setText(clientResponse.getData().get(0).getOther());

//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
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
//            AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
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
        //dialog.dismiss();
//        Log.d(TAG, "Error "+error);
//        AlertDialog.Builder ad = new AlertDialog.Builder(WriteActivity.this);
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
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

//        readTag.setText("NFC Content: " + text);
        try {
            JSONObject jsonObject = new JSONObject(text);  //"{\"phonetype\":\"N95\",\"cat\":\"WP\"}"
            //Toast.makeText(getApplicationContext(), jsonObject.get("Name")+"", Toast.LENGTH_LONG).show();
            //readTag.setText(jsonObject.get("Name")+"");
        }catch (JSONException err){
            Log.d("Error", err.toString());
        }
    }
//
    private String validate() {
        //boolean isValid = true;

        String isValid = null;

        if (TextUtils.isEmpty(cell.getText().toString().trim())) {
            isValid = "Please enter cell number";
            return isValid;
        }

//        if (TextUtils.isEmpty(zipcode.getText().toString().trim())) {
//            isValid = "Please enter zip code";
//            return isValid;
//        }

        if((cell.getText().toString().trim().length()>9 & cell.getText().toString().trim().length()<=20) & cell.getText().toString().matches("^(?=(?:\\D*\\d){10,18}\\D*$)(?:\\(?0?[0-9]{1,3}\\)?|\\+?[0-9]{1,3})[\\s-]?(?:\\(0?[0-9]{1,5}\\)|[0-9]{1,5})[-\\s]?[0-9][\\d\\s-]{5,7}\\s?(?:x[\\d-]{0,4})?(?:[-\\s]?[0-9]{1,4}|[-\\s])$")){

        }else{
            isValid = "Please enter valid cell number";
            return isValid;
        }
//
        if (TextUtils.isEmpty(name.getText().toString().trim())) {
            isValid = "Please enter first name";
            return isValid;
        }


        if (TextUtils.isEmpty(lastname.getText().toString().trim())) {
            isValid = "Please enter last name";
            return isValid;
        }
//        if (TextUtils.isEmpty(address.getText().toString().trim())) {
//            isValid = "Please enter address";
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
//        if(stateValue.length()<1){
//            isValid = "Please choose a state";
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
//        if (TextUtils.isEmpty(workNumberEnterprise.getText().toString().trim())) {
//            isValid = "Please enter work number enterprise";
//            return isValid;
//        }
//        if (TextUtils.isEmpty(company.getText().toString().trim())) {
//            isValid = "Please enter company name";
//            return isValid;
//        }
        return isValid;
    }

//    public void onItemSelected(AdapterView<?> parent, View view,
//                               int pos, long id) {
//        switch (parent.getId()) {
//            case R.id.spinCategory:
//                if(pos==0){
//                    category = "";
//                }else {
//                    category = categoryList.get(pos-1).getDescription();
//                }
//                //Toast.makeText(getApplicationContext(), category, Toast.LENGTH_LONG).show();
//                break;
//            case R.id.spinContactType:
//                if(pos==0){
//                    contactType = "";
//                }else {
//                    contactType = contactTypeList.get(pos-1).getDescription();
//                }
//                //Toast.makeText(getApplicationContext(), contactType, Toast.LENGTH_LONG).show();
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> arg0) {
//        // TODO Auto-generated method stub
//    }

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
        WriteModeOn();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */

        //setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        super.onPause();
        WriteModeOff();
        //stopForegroundDispatch(this, nfcAdapter);
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
//
//    /******************************************************************************
//     **********************************Write to NFC Tag****************************
//     ******************************************************************************/

    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

}
