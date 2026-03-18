package room;
import com.google.firebase.firestore.Query;
import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import api.Resource;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import supabase.SupabaseClient;
import supabase.SupabaseStorageApi;

public class LocalRepository {

    // Eliminamos MediaDao y Executor porque ya no usamos Room
    private String currentUserId;
    private FirebaseFirestore db;
    private SupabaseStorageApi storageApi;

    // Listeners y LiveDatas
    private ListenerRegistration pendientesListener;
    private final MutableLiveData<Resource<List<MediaEntity>>> pendientesLiveData = new MutableLiveData<>();

    private ListenerRegistration seguimientosListener;
    private final MutableLiveData<Resource<List<MediaEntity>>> seguimientosLiveData = new MutableLiveData<>();

    public LocalRepository(Application application) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "";
        }

        db = FirebaseFirestore.getInstance();

        // Inicializamos la API de Supabase
        storageApi = SupabaseClient.getClient().create(SupabaseStorageApi.class);
    }

    // =========================================================================
    // 1. SUPABASE (SUBIDA DE IMÁGENES PARA SEGUIMIENTOS)
    // =========================================================================

    public LiveData<Resource<List<MediaEntity>>> obtenerSeguimientosFiltrados(
            String queryTitulo, Float minPuntuacion, Float maxPuntuacion,
            Long fechaDesde, Long fechaHasta,
            String campoOrden, Query.Direction direccionOrden) {

        MutableLiveData<Resource<List<MediaEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        if (currentUserId.isEmpty()) {
            result.postValue(Resource.error("Usuario no autenticado"));
            return result;
        }

        Query query = db.collection("users").document(currentUserId).collection("seguimientos");

        boolean tieneFiltroTitulo = (queryTitulo != null && !queryTitulo.trim().isEmpty());

        // 1. Filtro por título
        if (tieneFiltroTitulo) {
            String textoBusqueda = queryTitulo.trim();
            query = query.whereGreaterThanOrEqualTo("titulo", textoBusqueda)
                    .whereLessThanOrEqualTo("titulo", textoBusqueda + '\uf8ff');
        }

        // 2. Filtro de Puntuación
        if (minPuntuacion != null) {
            query = query.whereGreaterThanOrEqualTo("puntuacion", minPuntuacion);
        }
        if (maxPuntuacion != null) {
            query = query.whereLessThanOrEqualTo("puntuacion", maxPuntuacion);
        }

        // 3. Ordenación Inteligente
        if (campoOrden != null && direccionOrden != null) {
            // REGLA DE FIRESTORE: Si buscamos por título, el primer 'orderBy' DEBE ser el título
            if (tieneFiltroTitulo && !campoOrden.equals("titulo")) {
                query = query.orderBy("titulo", Query.Direction.ASCENDING);
            }
            // Después aplicamos el orden que el usuario ha pedido (fechas o puntuación)
            query = query.orderBy(campoOrden, direccionOrden);
        }

        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                result.setValue(Resource.error("Error aplicando filtros: " + e.getMessage()));
                return;
            }
            if (snapshots != null) {
                List<MediaEntity> lista = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    MediaEntity media = doc.toObject(MediaEntity.class);
                    if (media != null) lista.add(media);
                }
                result.setValue(Resource.success(lista));
            }
        });


        return result;
    }
    public LiveData<Resource<String>> uploadImage(File imageFile) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Call<Void> call = storageApi.uploadImage("imagen", imageFile.getName(), body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String fileUrl = response.raw().request().url().toString();
                    result.postValue(Resource.success(fileUrl));
                } else {
                    result.postValue(Resource.error("Error al subir a Supabase"));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error(t.getMessage()));
            }
        });
        return result;
    }

    // En LocalRepository.java o AuthRepository.java
    public LiveData<Resource<String>> uploadFotoPerfil(File imageFile) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "profile_" + System.currentTimeMillis() + ".jpg", requestFile);

        // Usamos el bucket "imagen" que ya tienes configurado
        Call<Void> call = storageApi.uploadImage("imagen", "profile_" + System.currentTimeMillis() + ".jpg", body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String fileUrl = response.raw().request().url().toString().replace("/object/", "/object/public/");
                    result.postValue(Resource.success(fileUrl));
                } else {
                    result.postValue(Resource.error("Error al subir foto de perfil"));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error(t.getMessage()));
            }
        });
        return result;
    }

    // =========================================================================
    // 2. SEGUIMIENTOS (MIGRADO DE ROOM A FIRESTORE)
    // =========================================================================
    public LiveData<Resource<Boolean>> insertarSeguimientoSiNoExiste(MediaEntity media) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        if (currentUserId.isEmpty()) {
            result.postValue(Resource.error("Usuario no autenticado"));
            return result;
        }
        media.setUserId(currentUserId);

        db.collection("users").document(currentUserId).collection("seguimientos")
                .whereEqualTo("tmdbId", media.getTmdbId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        result.postValue(Resource.error("Ya existe en seguimiento"));
                    } else {
                        String idGenerado = db.collection("users").document(currentUserId).collection("seguimientos").document().getId();
                        media.setId(idGenerado);
                        db.collection("users").document(currentUserId).collection("seguimientos").document(idGenerado)
                                .set(media)
                                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage())));
                    }
                });
        return result;
    }

    public LiveData<Resource<List<MediaEntity>>> obtenerSeguimientos() {
        if (seguimientosListener == null && !currentUserId.isEmpty()) {
            seguimientosLiveData.setValue(Resource.loading());
            seguimientosListener = db.collection("users").document(currentUserId)
                    .collection("seguimientos")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            seguimientosLiveData.setValue(Resource.error("Error al obtener seguimientos"));
                            return;
                        }
                        if (snapshots != null) {
                            List<MediaEntity> lista = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                MediaEntity media = doc.toObject(MediaEntity.class);
                                if (media != null) lista.add(media);
                            }
                            seguimientosLiveData.setValue(Resource.success(lista));
                        }
                    });
        }
        return seguimientosLiveData;
    }

    public void eliminarSeguimiento(MediaEntity media) {
        if (!currentUserId.isEmpty() && media.getId() != null) {
            db.collection("users").document(currentUserId)
                    .collection("seguimientos").document(media.getId())
                    .delete();
        }
    }

    // =========================================================================
    // 3. PENDIENTES (INTACTO, TAL CUAL LO TENÍAS TÚ)
    // =========================================================================

    public void eliminarPendiente(MediaEntity media) {
        if (!currentUserId.isEmpty() && media.getTmdbId() != 0) {
            // Borramos el documento usando el tmdbId, que es como lo guardaste en pendientes
            db.collection("users").document(currentUserId)
                    .collection("pendientes").document(String.valueOf(media.getTmdbId()))
                    .delete();
        }
    }
    public void insertarPendienteSiNoExiste(MediaEntity media, InsertCallback callback) {
        if (currentUserId.isEmpty()) return;
        media.setUserId(currentUserId);

        db.collection("users").document(currentUserId)
                .collection("pendientes").document(String.valueOf(media.getTmdbId()))
                .set(media)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public LiveData<Resource<List<MediaEntity>>> obtenerPendientes() {
        if (pendientesListener == null && !currentUserId.isEmpty()) {
            pendientesLiveData.setValue(Resource.loading());
            pendientesListener = db.collection("users").document(currentUserId)
                    .collection("pendientes")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            pendientesLiveData.setValue(Resource.error("Error al obtener pendientes"));
                            return;
                        }
                        if (snapshots != null) {
                            List<MediaEntity> lista = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                MediaEntity media = doc.toObject(MediaEntity.class);
                                if (media != null) lista.add(media);
                            }
                            pendientesLiveData.setValue(Resource.success(lista));
                        }
                    });
        }
        return pendientesLiveData;
    }

    public LiveData<MediaEntity> obtenerPorId(String id) {
        MutableLiveData<MediaEntity> liveData = new MutableLiveData<>();
        if (!currentUserId.isEmpty() && id != null) {
            db.collection("users").document(currentUserId)
                    .collection("seguimientos").document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            MediaEntity media = documentSnapshot.toObject(MediaEntity.class);
                            liveData.setValue(media);
                        }
                    });
        }
        return liveData;
    }

    public void stopListening() {
        if (pendientesListener != null) {
            pendientesListener.remove();
            pendientesListener = null;
        }
        if (seguimientosListener != null) {
            seguimientosListener.remove();
            seguimientosListener = null;
        }
    }

    public interface InsertCallback {
        void onResult(boolean success);
    }
}