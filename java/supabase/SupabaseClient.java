package supabase;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://gljhrhlqcapllxqydwza.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdsamhyaGxxY2FwbGx4cXlkd3phIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM3NTQ3NTQsImV4cCI6MjA4OTMzMDc1NH0.9QejACgi69qYSwipzNncayWAuxkB6idsA6eFiJRMXuc";

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new SupabaseAuthInterceptor(API_KEY))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}