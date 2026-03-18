package data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Serie implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
}