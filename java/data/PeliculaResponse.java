package data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PeliculaResponse {
    @SerializedName("results")
    private List<Pelicula> results;

    public List<Pelicula> getResults() {
        return results;
    }
}