package api;

import data.Pelicula;
import data.PeliculaResponse;
import data.Serie;
import data.SerieResponse;
import data.VideoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApi {

    @GET("movie/popular")
    Call<PeliculaResponse> getPeliculasPopulares(@Query("page") int page, @Query("language") String language);

    @GET("movie/{id}")
    Call<Pelicula> getDetallePelicula(@Path("id") int id, @Query("language") String language);

    @GET("movie/{id}/videos")
    Call<VideoResponse> getVideosPelicula(@Path("id") int id);


    @GET("tv/popular")
    Call<SerieResponse> getSeriesPopulares(@Query("page") int page, @Query("language") String language);

    @GET("tv/{id}")
    Call<Serie> getDetalleSerie(@Path("id") int id,@Query("language") String language);

    @GET("tv/{id}/videos")
    Call<VideoResponse> getVideosSerie(@Path("id") int id);

    @GET("search/movie")
    Call<PeliculaResponse> searchPeliculas(@Query("query") String query,@Query("language") String language);

    @GET("search/tv")
    Call<SerieResponse> searchSeries(@Query("query") String query,@Query("language") String language);

}