package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import api.Resource;
import data.Pelicula;
import data.Serie;
import data.Video;
import repository.TmdbRepository;

public class TmdbViewModel extends AndroidViewModel {
    private final TmdbRepository repository;

    public MutableLiveData<Resource<Pelicula>> peliculaSeleccionada = new MutableLiveData<>();
    public MutableLiveData<Resource<Serie>> serieSeleccionada = new MutableLiveData<>();

    public MutableLiveData<Resource<List<Video>>> videos = new MutableLiveData<>();

    public MutableLiveData<Resource<List<Pelicula>>> listaPeliculas = new MutableLiveData<>();
    public MutableLiveData<Resource<List<Serie>>> listaSeries = new MutableLiveData<>();

    private final List<Pelicula> peliculasAcumuladas = new ArrayList<>();
    private final List<Serie> seriesAcumuladas = new ArrayList<>();

    public MutableLiveData<Resource<List<Pelicula>>> resultadosBusquedaPeliculas = new MutableLiveData<>();
    public MutableLiveData<Resource<List<Serie>>> resultadosBusquedaSeries = new MutableLiveData<>();

    private boolean isLoadingPeliculas = false;
    private boolean isLoadingSeries = false;

    public TmdbViewModel(@NonNull Application application) {
        super(application);
        repository = new TmdbRepository(application);
    }

    public void seleccionarPelicula(int id) {
        repository.getDetallePelicula(id, result -> {
            peliculaSeleccionada.postValue(result);
        });

        repository.getVideosPelicula(id, result -> {
            videos.postValue(result);
        });
    }

    public void seleccionarSerie(int id) {
        repository.getDetalleSerie(id, result -> {
            serieSeleccionada.postValue(result);
        });

        repository.getVideosSerie(id, result -> {
            videos.postValue(result);
        });
    }

    public void cargarPeliculas() {
        if (isLoadingPeliculas) return;
        isLoadingPeliculas = true;

        repository.getPeliculas(result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                peliculasAcumuladas.addAll(result.data);
                listaPeliculas.postValue(Resource.success(new ArrayList<>(peliculasAcumuladas)));
            }
            else {
                listaPeliculas.postValue(result);
            }

            switch (result.status) {
                case ERROR:
                case SUCCESS:
                    isLoadingPeliculas = false;
            }
        });
    }

    public void cargarSeries() {
        if (isLoadingSeries) return;
        isLoadingSeries = true;

        repository.getSeries(result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                seriesAcumuladas.addAll(result.data);
                listaSeries.postValue(Resource.success(new ArrayList<>(seriesAcumuladas)));
            }
            else {
                listaSeries.postValue(result);
            }

            switch (result.status) {
                case ERROR:
                case SUCCESS:
                    isLoadingSeries = false;
            }
        });
    }

    public void buscarPeliculas(String query) {
        repository.searchPeliculas(query, result -> resultadosBusquedaPeliculas.postValue(result));
    }

    public void buscarSeries(String query) {
        repository.searchSeries(query, result -> resultadosBusquedaSeries.postValue(result));
    }


    public void limpiarDatos() {
        peliculasAcumuladas.clear();
        seriesAcumuladas.clear();
        repository.resetearPaginacion();

        listaPeliculas.setValue(null);
        listaSeries.setValue(null);
    }
}
