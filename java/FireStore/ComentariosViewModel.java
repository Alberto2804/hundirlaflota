package FireStore;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import api.Resource;
import FireStore.Comentario;
import FireStore.ComentariosRepository;

public class ComentariosViewModel extends AndroidViewModel {
    private ComentariosRepository repository;

    public ComentariosViewModel(@NonNull Application application) {
        super(application);
        repository = new ComentariosRepository();
    }

    public LiveData<Resource<List<Comentario>>> getComentarios(int tmdbId) {
        return repository.obtenerComentarios(tmdbId);
    }

    public void agregarComentario(int tmdbId, Comentario c) {
        repository.agregarComentario(tmdbId, c);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}
