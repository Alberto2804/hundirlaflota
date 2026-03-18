package sharedPreferences;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

    private final PreferencesRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new PreferencesRepository(application);
    }

    public String getNombre() { return repository.getNombreUsuario(); }
    public void guardarNombre(String nombre) { repository.setNombreUsuario(nombre); }

    public String getIdioma() { return repository.getIdioma(); }
    public void guardarIdioma(String idioma) { repository.setIdioma(idioma); }

    public boolean isSoloWifi() { return repository.isSoloWifi(); }
    public void guardarSoloWifi(boolean soloWifi) { repository.setSoloWifi(soloWifi); }

    public String getTema() { return repository.getTema(); }
    public void guardarTema(String tema) { repository.setTema(tema); }

    public void resetear() { repository.resetearPreferencias(); }
}
