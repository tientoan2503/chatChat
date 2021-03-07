package com.example.chatchat.fragment;

import com.example.chatchat.notification.MyResponse;
import com.example.chatchat.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAABtV86sY:APA91bHkhUHf5Ng9qaU-VoByHd6MhsSI9Y38OXBv3DD5X36TBnR7vbnL8tqbNsKjIMTcophImJR04fR4k_9yDIvuYpcya8XtudfszFbBsVsSv8c05JPiTKjwwiGUzDyESEJr7XoQAm9C"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
