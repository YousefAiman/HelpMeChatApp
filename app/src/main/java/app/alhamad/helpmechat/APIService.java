package app.alhamad.helpmechat;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAANa8tS4k:APA91bG2kbfp6OiS20SGOZekVxapaS4k3jTFiQx-pyrikYKvCN-_y3HI2Iwd9DLu8XSQybvQ3BBYrq4hinIvDwFrtfsophSuzjJ8NJgpJjkCuVqRBNBAroJAqPOy6uRS4ETFRGnjwDOx"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
