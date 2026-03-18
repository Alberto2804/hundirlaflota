package repository;

import android.content.Context;

import java.util.List;

import api.Resource;
import api.RetrofitClient;
import api.TmdbApi;
import data.Pelicula;
import data.PeliculaResponse;
import data.Serie;
import data.SerieResponse;
import data.Video;
import data.VideoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sharedPreferences.PreferencesRepository;

public class TmdbRepository {
    private int pagePeliculas = 1;
    private int pageSeries = 1;

    private final TmdbApi api;
    private final PreferencesRepository preferences;

    public TmdbRepository(Context context) {
        api = RetrofitClient.getApi();
        preferences = new PreferencesRepository(context);
    }

    private String getIdioma() {
        return preferences.getIdioma();
    }

    public interface PeliculasCallback {
        void onResult(Resource<List<Pelicula>> result);
    }
    public interface SeriesCallback {
        void onResult(Resource<List<Serie>> result);
    }

    public interface DetallePeliculaCallback {
        void onResult(Resource<Pelicula> result);
    }

    public interface DetalleSerieCallback {
        void onResult(Resource<Serie> result);
    }

    public interface VideosCallback {
        void onResult(Resource<List<Video>> result);
    }

    public void getPeliculas(PeliculasCallback callback) {
        callback.onResult(Resource.loading());
        api.getPeliculasPopulares(pagePeliculas, getIdioma()).enqueue(new Callback<PeliculaResponse>() {
            @Override
            public void onResponse(Call<PeliculaResponse> call, Response<PeliculaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pelicula> lista = response.body().getResults();
                    callback.onResult(Resource.success(lista));
                    pagePeliculas++;
                } else {
                    callback.onResult(Resource.error("No se pudieron cargar las películas"));
                }
            }

            @Override
            public void onFailure(Call<PeliculaResponse> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getSeries(SeriesCallback callback) {
        callback.onResult(Resource.loading());
        api.getSeriesPopulares(pageSeries, getIdioma()).enqueue(new Callback<SerieResponse>() {
            @Override
            public void onResponse(Call<SerieResponse> call, Response<SerieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Serie> lista = response.body().getResults();
                    callback.onResult(Resource.success(lista));
                    pageSeries++;
                } else {
                    callback.onResult(Resource.error("No se pudieron cargar las series"));
                }
            }

            @Override
            public void onFailure(Call<SerieResponse> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getDetallePelicula(int id, DetallePeliculaCallback callback) {
        callback.onResult(Resource.loading());
        api.getDetallePelicula(id, getIdioma()).enqueue(new Callback<Pelicula>() {
            @Override
            public void onResponse(Call<Pelicula> call, Response<Pelicula> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Película no encontrada"));
                }
            }

            @Override
            public void onFailure(Call<Pelicula> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getDetalleSerie(int id, DetalleSerieCallback callback) {
        callback.onResult(Resource.loading());
        api.getDetalleSerie(id, getIdioma()).enqueue(new Callback<Serie>() {
            @Override
            public void onResponse(Call<Serie> call, Response<Serie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body()));
                } else {
                    callback.onResult(Resource.error("Serie no encontrada"));
                }
            }

            @Override
            public void onFailure(Call<Serie> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getVideosPelicula(int id, VideosCallback callback) {
        callback.onResult(Resource.loading());
        api.getVideosPelicula(id).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body().getResults()));
                } else {
                    callback.onResult(Resource.error("Error al cargar vídeos"));
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getVideosSerie(int id, VideosCallback callback) {
        callback.onResult(Resource.loading());
        api.getVideosSerie(id).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body().getResults()));
                } else {
                    callback.onResult(Resource.error("Error al cargar vídeos"));
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                callback.onResult(Resource.error("Error de red: " + t.getMessage()));
            }
        });
    }

    public void searchPeliculas(String query, PeliculasCallback callback) {
        api.searchPeliculas(query, getIdioma()).enqueue(new Callback<PeliculaResponse>() {
            @Override
            public void onResponse(Call<PeliculaResponse> call, Response<PeliculaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body().getResults()));
                } else {
                    callback.onResult(Resource.error("Error buscando películas"));
                }
            }
            @Override
            public void onFailure(Call<PeliculaResponse> call, Throwable t) {
                callback.onResult(Resource.error(t.getMessage()));
            }
        });
    }

    public void searchSeries(String query, SeriesCallback callback) {
        api.searchSeries(query, getIdioma()).enqueue(new Callback<SerieResponse>() {
            @Override
            public void onResponse(Call<SerieResponse> call, Response<SerieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(Resource.success(response.body().getResults()));
                } else {
                    callback.onResult(Resource.error("Error buscando series"));
                }
            }

            @Override
            public void onFailure(Call<SerieResponse> call, Throwable t) {
                callback.onResult(Resource.error(t.getMessage()));
            }
        });
    }
    public void resetearPaginacion() {
        this.pagePeliculas = 1;
        this.pageSeries = 1;
    }
}