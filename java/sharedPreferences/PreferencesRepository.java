package sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesRepository {

    private static final String PREFS_NAME = "prefs";
    private static final String KEY_NOMBRE = "nombre_usuario";
    private static final String KEY_IDIOMA = "idioma";
    private static final String KEY_SOLO_WIFI = "solo_wifi";
    private static final String KEY_TEMA = "tema";

    private final SharedPreferences sharedPreferences;

    public PreferencesRepository(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    public String getNombreUsuario() {
        return sharedPreferences.getString(KEY_NOMBRE, "");
    }

    public void setNombreUsuario(String nombre) {
        sharedPreferences.edit().putString(KEY_NOMBRE, nombre).apply();
    }

    public String getIdioma() {
        // Por defecto español
        return sharedPreferences.getString(KEY_IDIOMA, "es-ES");
    }

    public void setIdioma(String idioma) {
        sharedPreferences.edit().putString(KEY_IDIOMA, idioma).apply();
    }

    public boolean isSoloWifi() {
        return sharedPreferences.getBoolean(KEY_SOLO_WIFI, false);
    }

    public void setSoloWifi(boolean soloWifi) {
        sharedPreferences.edit().putBoolean(KEY_SOLO_WIFI, soloWifi).apply();
    }

    public String getTema() {
        return sharedPreferences.getString(KEY_TEMA, "claro");
    }

    public void setTema(String tema) {
        sharedPreferences.edit().putString(KEY_TEMA, tema).apply();
    }



    public void resetearPreferencias() {
        sharedPreferences.edit().clear().apply();
        setTema("claro");
        setIdioma("es-ES");
    }
}
