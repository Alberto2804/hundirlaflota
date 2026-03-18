package data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SerieResponse {
    @SerializedName("results")
    private List<Serie> results;

    public List<Serie> getResults() {
        return results;
    }
}