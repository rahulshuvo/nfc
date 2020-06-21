package com.service.sohan.nfcreadwritetest.Api;

import com.service.sohan.nfcreadwritetest.Model.CategoryResponse;
import com.service.sohan.nfcreadwritetest.Model.ContactTypeResponse;
import com.service.sohan.nfcreadwritetest.Model.ProfileResponse;
import com.service.sohan.nfcreadwritetest.Model.ReaderDataPostedResponse;
import com.service.sohan.nfcreadwritetest.Model.StateResponse;
import com.service.sohan.nfcreadwritetest.Model.VersionResponse;
import com.service.sohan.nfcreadwritetest.Model.WriterPostedResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    // post writer data
    @GET("contact.ashx?datatype=add_user_write")
    Observable<WriterPostedResponse> postWriteData(@Query("p") String phone,
                                                   @Query("e") String email,
                                                   @Query("Data") String Data);

    // post reader data
    @GET("contact.ashx?datatype=add_contact_read")
    Observable<ReaderDataPostedResponse> postReaderData(@Query("shared") String shared,
                                                        @Query("name") String name,
                                                        @Query("fp") String fp,
                                                        @Query("tp") String tp,
                                                        @Query("Data") String Data);

    // post reader data
    @GET("contact.ashx?datatype=get_states")
    Observable<StateResponse> getStates();

    // get contact types
    @GET("contact.ashx?datatype=get_contactTypes")
    Observable<ContactTypeResponse> getContactType();

    // get category types
    @GET("contact.ashx?datatype=get_categories")
    Observable<CategoryResponse> getCategoryType();

    // get profile types
    @GET("contact.ashx?datatype=get_user")
    Observable<ProfileResponse> getProfile(@Query("p") String p);

    // get version
    @GET("contact.ashx?datatype=get_version")
    Observable<VersionResponse> getVersion();

}
