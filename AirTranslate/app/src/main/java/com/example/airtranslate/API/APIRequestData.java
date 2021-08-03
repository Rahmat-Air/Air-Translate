package com.example.airtranslate.API;

import com.example.airtranslate.Model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIRequestData {
    @GET("translate.php")
    Call<ResponseModel> ardKirimTranslate(@Query("text") String text,
                                          @Query("from") String from,
                                          @Query("to") String to);


}
