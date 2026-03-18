package auth;

import com.google.firebase.auth.FirebaseUser;

public class AuthState {
    public boolean loading;
    public FirebaseUser user;
    public String error;

    public static AuthState loading() {
        AuthState s = new AuthState();
        s.loading = true;
        return s;
    }

    public static AuthState success(FirebaseUser user) {
        AuthState s = new AuthState();
        s.user = user;
        return s;
    }

    public static AuthState error(String msg) {
        AuthState s = new AuthState();
        s.error = msg;
        return s;
    }
}