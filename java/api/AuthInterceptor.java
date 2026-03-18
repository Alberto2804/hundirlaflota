package api;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhMWI1ZjJkOWE5ZjU1OGI1ZDI0Y2YwZWEwODVhNTQ1ZiIsIm5iZiI6MTc2ODIzOTMyNi4yNTYsInN1YiI6IjY5NjUzMGRlNTRmOTBhYmVhNmIzNTM2OSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.nPgpoycmNDqXIt86REyRnD_VXFL-OMBqOZEvO_c4Mo8";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request original = chain.request();



        Request request = original.newBuilder()
                .header("Authorization", "Bearer " + TOKEN)
                .header("accept", "application/json")
                .build();

        return chain.proceed(request);
    }
}

