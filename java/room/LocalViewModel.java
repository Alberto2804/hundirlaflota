package room;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.List;

import api.Resource;

public class LocalViewModel extends AndroidViewModel {

    private LocalRepository repository;

    public LocalViewModel(@NonNull Application application) {
        super(application);
        repository = new LocalRepository(application);
    }

    // --- NUEVO: SUPABASE ---
    public LiveData<Resource<List<MediaEntity>>> obtenerSeguimientosFiltrados(
            String titulo, Float minPunt, Float maxPunt,
            Long fechaDesde, Long fechaHasta,
            String campoOrden, Query.Direction direccionOrden) {

        return repository.obtenerSeguimientosFiltrados(titulo, minPunt, maxPunt, fechaDesde, fechaHasta, campoOrden, direccionOrden);
    }
    public LiveData<Resource<String>> uploadImage(Uri imageUri) {
        try {
            File file = ImageUtils.getFileFromUri(getApplication(), imageUri);
            return repository.uploadImage(file);
        } catch (Exception e) {
            MutableLiveData<Resource<String>> errorRes = new MutableLiveData<>();
            errorRes.setValue(Resource.error("Error procesando imagen"));
            return errorRes;
        }
    }

    public LiveData<MediaEntity> obtenerPorId(String id) {
        return repository.obtenerPorId(id);
    }

    // --- NUEVO: SEGUIMIENTOS EN FIRESTORE ---
    public LiveData<Resource<Boolean>> insertarSeguimientoUnico(MediaEntity media) {
        return repository.insertarSeguimientoSiNoExiste(media);
    }

    public LiveData<Resource<List<MediaEntity>>> obtenerSeguimientos() {
        return repository.obtenerSeguimientos();
    }

    public void eliminarSeguimiento(MediaEntity media) {
        repository.eliminarSeguimiento(media);
    }

    // --- INTACTO: TUS PENDIENTES ---

    public void eliminarPendiente(MediaEntity media) {
        repository.eliminarPendiente(media);
    }
    public void insertarPendienteUnico(MediaEntity media, LocalRepository.InsertCallback callback) {
        repository.insertarPendienteSiNoExiste(media, callback);
    }

    public LiveData<Resource<List<MediaEntity>>> obtenerPendientes() {
        return repository.obtenerPendientes();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}