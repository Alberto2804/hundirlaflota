package auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repo = new AuthRepository();
    }

    public MutableLiveData<AuthState> getAuthState() { return authState; }
    public FirebaseUser getCurrentUser() { return repo.getCurrentUser(); }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            authState.setValue(AuthState.error("Todos los campos son obligatorios."));
            return;
        }
        authState.setValue(AuthState.loading());
        repo.login(email.trim(), password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(FirebaseUser user) { authState.postValue(AuthState.success(user)); }
            @Override public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }

    public void register(String email, String password, String confirmPass, String username, String fotoUrl) {

        String emailRegex = "^[^@]+@[^@]+\\.[^@]+$";
        if (!email.matches(emailRegex)) {
            authState.setValue(AuthState.error("Formato de correo inválido. Asegúrate de incluir '@' y un dominio."));
            return;
        }

        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*") || !password.equals(confirmPass)) {
            authState.setValue(AuthState.error("Revisa que la contraseña tenga 8 caracteres, letras, números y ambas coincidan."));
            return;
        }

        authState.setValue(AuthState.loading());
        repo.register(email.trim(), password, username.trim(), fotoUrl, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(FirebaseUser user) { authState.postValue(AuthState.success(user)); }
            @Override public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }

    public void loginWithGoogle(String idToken) {
        authState.setValue(AuthState.loading());
        repo.loginWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(FirebaseUser user) { authState.postValue(AuthState.success(user)); }
            @Override public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }

    public void resetPassword(String email) {
        if(email.isEmpty()) {
            authState.setValue(AuthState.error("Introduce tu correo para restablecer la contraseña."));
            return;
        }
        repo.resetPassword(email, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(FirebaseUser user) { authState.postValue(AuthState.error("Correo de recuperación enviado.")); }
            @Override public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }
}