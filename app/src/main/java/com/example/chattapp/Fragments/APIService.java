package com.example.chattapp.Fragments;

import com.example.chattapp.Notification.MyResponse;
import com.example.chattapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {"Content-Type:application/json",
                    "Authorization:key=AAAADuh23GM:APA91bENOW2Qgb6wGv08T5y-5xsz19xXQbp52em22FQLLIODsMzMnim2HtjW7O7RDoScuJzJuRGuZNCBbWuwW8t1fXzzASIC0lS5gJgewAp57LvIL4uxaFHR2-RtupMcwQQ9ajgARtdU"

            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
