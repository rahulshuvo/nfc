package com.service.sohan.nfcreadwritetest.Api;

import android.content.Context;

public class ApiUtils {
    private static Context mContext;
    // public static final String BASE_URL = "http://173.248.132.45/API/";
    public static final String BASE_URL = "https://etag365.net/API/";

    public static ApiInterface getService() {
        return RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
    }

}
