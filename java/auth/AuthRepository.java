package auth;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import FireStore.Usuario;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(auth.getCurrentUser()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String email, String password, String username, String fotoUrl, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {

                        Usuario nuevoUsuario = new Usuario(username, email, System.currentTimeMillis());

                        // Si nos llega una URL, la ponemos. Si no, la de por defecto.
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            nuevoUsuario.setFotoPerfilUrl(fotoUrl);
                        } else {
                            nuevoUsuario.setFotoPerfilUrl("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png");
                        }

                        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid())
                                .set(nuevoUsuario)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(firebaseUser))
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users").document(firebaseUser.getUid()).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        String nombreGoogle = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario de Google";
                                        Usuario nuevoUsuario = new Usuario(nombreGoogle, firebaseUser.getEmail(), System.currentTimeMillis());

                                        if (firebaseUser.getPhotoUrl() != null) {
                                            nuevoUsuario.setFotoPerfilUrl(firebaseUser.getPhotoUrl().toString());
                                        }

                                        db.collection("users").document(firebaseUser.getUid())
                                                .set(nuevoUsuario)
                                                .addOnSuccessListener(aVoid -> callback.onSuccess(firebaseUser))
                                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                    } else {
                                        callback.onSuccess(firebaseUser);
                                    }
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void resetPassword(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }
}