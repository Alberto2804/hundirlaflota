package com.example.proyectoenero;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

// IMPORTAMOS GLIDE
import com.bumptech.glide.Glide;

import com.example.proyectoenero.databinding.FragmentAjustesBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import auth.AuthActivity;
import room.LocalRepository;
import sharedPreferences.MainViewModel;
import viewmodel.TmdbViewModel;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private MainViewModel viewModel;

    private final List<String> idiomasNombres = Arrays.asList("Español (España)", "Inglés (USA)", "Francés", "Alemán");
    private final List<String> idiomasCodigos = Arrays.asList("es-ES", "en-US", "fr-FR", "de-DE");

    private ActivityResultLauncher<String> selectorImagenLauncher;
    private Uri nuevaFotoUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaFotoUri = uri;
                        ImageView iv = binding.getRoot().findViewById(R.id.ivFotoPerfilAjustes);
                        if (iv != null) iv.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupSpinner();
        cargarPreferencias();

        View btnCambiarFoto = binding.getRoot().findViewById(R.id.btnCambiarFoto);
        if (btnCambiarFoto != null) {
            btnCambiarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        }

        binding.btnGuardar.setOnClickListener(v -> guardarPreferencias());

        binding.btnReset.setOnClickListener(v -> {
            viewModel.resetear();
            cargarPreferencias();
            aplicarTema("claro");
            mostrarDialogo( "Éxito" ,"Preferencias reseteadas");
        });

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(requireActivity(), gso);
            googleClient.signOut();

            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, idiomasNombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerIdioma.setAdapter(adapter);
    }

    private void cargarPreferencias() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        if (doc.contains("displayName")) {
                            binding.etNombreUsuario.setText(doc.getString("displayName"));
                        }
                        // AQUÍ ES DONDE GLIDE CARGA LA FOTO DESDE SUPABASE
                        if (doc.contains("fotoPerfilUrl")) {
                            String url = doc.getString("fotoPerfilUrl");
                            ImageView iv = binding.getRoot().findViewById(R.id.ivFotoPerfilAjustes);
                            if (iv != null && isAdded() && getContext() != null) {
                                // Cargamos la URL de internet directamente en el ImageView
                                Glide.with(requireContext()).load(url).into(iv);
                            }
                        }
                    }
                });

        binding.etNombreUsuario.setText(viewModel.getNombre());
        int index = idiomasCodigos.indexOf(viewModel.getIdioma());
        if (index >= 0) binding.spinnerIdioma.setSelection(index);
        binding.cbSoloWifi.setChecked(viewModel.isSoloWifi());

        String tema = viewModel.getTema();
        if ("oscuro".equals(tema)) binding.rbTemaOscuro.setChecked(true);
        else binding.rbTemaClaro.setChecked(true);
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar_ajustes", ".jpg", requireContext().getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) { out.write(buffer, 0, read); }
            out.close();
            in.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void guardarPreferencias() {
        String nuevoNombre = binding.etNombreUsuario.getText().toString();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).update("displayName", nuevoNombre);

        if (nuevaFotoUri != null) {
            File file = uriToFile(nuevaFotoUri);
            if(file != null) {

                LocalRepository localRepo = new LocalRepository(requireActivity().getApplication());
                localRepo.uploadFotoPerfil(file).observe(getViewLifecycleOwner(), resource -> {
                    if (resource != null) {
                        if (resource.data != null) {
                            db.collection("users").document(uid).update("fotoPerfilUrl", resource.data);
                            mostrarDialogo("Éxito","Foto actualizada en la nube");
                            nuevaFotoUri = null;
                        } else if (resource.message != null) {
                            mostrarDialogo("Error", "Error subida: " + resource.message);
                        }
                    }
                });
            }
        } else {
            mostrarDialogo("Información", "Cambios guardados");
        }

        viewModel.guardarNombre(nuevoNombre);
        viewModel.guardarIdioma(idiomasCodigos.get(binding.spinnerIdioma.getSelectedItemPosition()));
        viewModel.guardarSoloWifi(binding.cbSoloWifi.isChecked());

        String tema = binding.rbTemaOscuro.isChecked() ? "oscuro" : "claro";
        viewModel.guardarTema(tema);
        aplicarTema(tema);

        TmdbViewModel tmdbViewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);
        tmdbViewModel.limpiarDatos();
    }

    private void aplicarTema(String tema) {
        AppCompatDelegate.setDefaultNightMode("oscuro".equals(tema) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
    

    private void mostrarDialogo(String titulo, String mensaje) {
        if (getContext() != null) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(titulo)
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show();
        }
    }
}