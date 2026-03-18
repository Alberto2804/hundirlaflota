package com.example.proyectoenero;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import auth.AuthViewModel;
import com.example.proyectoenero.MainActivity;
import com.example.proyectoenero.R;
import com.example.proyectoenero.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        inicializarLauncherGoogleSignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarGoogleSignIn();
        observeAuthState();

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString();
            String pass = binding.passwordEditText.getText().toString();
            viewModel.login(email, pass);
        });

        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        binding.forgotPasswordTextView.setOnClickListener(v -> {
            viewModel.resetPassword(binding.emailEditText.getText().toString());
        });

        binding.registerTextView.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_register);
        });
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