package data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoResponse {
    @SerializedName("results")
    private List<Video> results;

    public List<Video> getResults() {
        return results;
    }
}