package FireStore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import api.Resource;
import FireStore.Comentario;

public class ComentariosRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;
    private final MutableLiveData<Resource<List<Comentario>>> comentariosLiveData = new MutableLiveData<>();

    public LiveData<Resource<List<Comentario>>> obtenerComentarios(int tmdbId) {
        if (listener == null) {
            comentariosLiveData.setValue(Resource.loading());
            listener = db.collection("multimedia").document(String.valueOf(tmdbId))
                    .collection("comments")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            comentariosLiveData.setValue(Resource.error("Error al cargar"));
                            return;
                        }
                        if (snapshots != null) {
                            List<Comentario> lista = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Comentario c = doc.toObject(Comentario.class);
                                if (c != null) lista.add(c);
                            }
                            comentariosLiveData.setValue(Resource.success(lista));
                        }
                    });
        }
        return comentariosLiveData;
    }

    public void agregarComentario(int tmdbId, Comentario c) {
        db.collection("multimedia").document(String.valueOf(tmdbId))
                .collection("comments").add(c);
    }

    public void stopListening() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
