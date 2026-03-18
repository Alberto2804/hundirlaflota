package com.example.proyectoenero;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import auth.AuthViewModel;
import room.LocalRepository;
import com.example.proyectoenero.databinding.FragmentRegisterBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private ActivityResultLauncher<String> selectorImagenLauncher;

    private Uri fotoPerfilUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        inicializarLauncherGoogleSignIn();

        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoPerfilUri = uri;
                        ImageView ivFoto = binding.getRoot().findViewById(R.id.ivFotoPerfil);
                        if(ivFoto != null) ivFoto.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configurarGoogleSignIn();
        observeAuthState();

        View btnSeleccionarFoto = binding.getRoot().findViewById(R.id.btnSeleccionarFoto);
        if(btnSeleccionarFoto != null) {
            btnSeleccionarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        }

        binding.registerButton.setOnClickListener(v -> {
            String username = binding.usernameEditText.getText().toString();
            String email = binding.emailEditText.getText().toString();
            String pass = binding.passwordEditText.getText().toString();
            String confirmPass = binding.confirmPasswordEditText.getText().toString();

            if (fotoPerfilUri != null) {
                File file = uriToFile(fotoPerfilUri);
                if (file != null) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.registerButton.setEnabled(false);

                    // USAMOS TU LOCAL REPOSITORY PARA SUBIR LA FOTO
                    LocalRepository localRepo = new LocalRepository(requireActivity().getApplication());
                    localRepo.uploadFotoPerfil(file).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null) {
                            if (resource.data != null) { // ÉXITO
                                viewModel.register(email, pass, confirmPass, username, resource.data);
                            } else if (resource.message != null) { // ERROR
                                mostrarDialogo( "Error", "Error subiendo foto: " + resource.message);
                                binding.progressBar.setVisibility(View.GONE);
                                binding.registerButton.setEnabled(true);
                            }
                        }
                    });
                }
            } else {
                // Sin foto, pasamos null para que coja la de por defecto
                viewModel.register(email, pass, confirmPass, username, null);
            }
        });

        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        binding.loginTextView.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    // MÉTODO AUXILIAR PARA CONVERTIR URI A FILE TEMPORAL
    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar_tmp", ".jpg", requireContext().getCacheDir());
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

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void inicializarLauncherGoogleSignIn() {
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            viewModel.loginWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            mostrarDialogo( "Error de Google", e.getMessage());
                        }
                    }
                }
        );
    }

    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            binding.registerButton.setEnabled(!state.loading);

            if (state.error != null) {
                mostrarDialogo( "Error de autenticación", state.error);
                state.error = null;
            }

            if (state.user != null) {
                startActivity(new Intent(requireContext(), MainActivity.class));
                requireActivity().finish();
            }
        });
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