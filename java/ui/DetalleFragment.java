package ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.proyectoenero.R;
import com.example.proyectoenero.databinding.FragmentDetalleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import FireStore.Comentario;
import FireStore.ComentarioAdapter;
import FireStore.ComentariosViewModel;
import api.Resource;
import data.Pelicula;
import data.Serie;
import data.Video;
import sharedPreferences.PreferencesRepository;
import viewmodel.TmdbViewModel;

public class DetalleFragment extends Fragment {

    private FragmentDetalleBinding binding;
    private TmdbViewModel viewModel;
    private String currentVideoKey = null;
    private PreferencesRepository prefs;

    private ComentariosViewModel comentariosViewModel;
    private String currentUserName = "Anónimo";
    private ComentarioAdapter comentarioAdapter;

    private int tmdbIdActual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetalleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new PreferencesRepository(requireContext());

        viewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);

        comentariosViewModel = new ViewModelProvider(this).get(ComentariosViewModel.class);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("displayName")) {
                            currentUserName = doc.getString("displayName");
                        }
                    });
        }


        comentarioAdapter = new ComentarioAdapter();
        binding.recyclerComentarios.setAdapter(comentarioAdapter);
        binding.recyclerComentarios.setLayoutManager(new LinearLayoutManager(getContext()));


        if (getArguments() != null) {
            tmdbIdActual = getArguments().getInt("id");
            boolean esPelicula = getArguments().getBoolean("esPelicula");


            comentariosViewModel.getComentarios(tmdbIdActual).observe(getViewLifecycleOwner(), resource -> {
                if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    comentarioAdapter.setLista(resource.data);
                    binding.tvComentariosTitulo.setText("Comentarios (" + resource.data.size() + ")");
                }
            });


            binding.btnEnviarComentario.setOnClickListener(v -> {
                String texto = binding.etNuevoComentario.getText().toString().trim();
                if (!texto.isEmpty() && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Comentario c = new Comentario(uid, currentUserName, texto, System.currentTimeMillis());
                    comentariosViewModel.agregarComentario(tmdbIdActual, c);
                    binding.etNuevoComentario.setText(""); // Limpiar la caja de texto
                } else if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    mostrarDialogo( "Aviso", "Debes iniciar sesión para comentar");
                }
            });


            if (esPelicula) {
                viewModel.seleccionarPelicula(tmdbIdActual);
                viewModel.peliculaSeleccionada.observe(getViewLifecycleOwner(), resource -> {
                    if (resource != null) {
                        switch (resource.status) {
                            case LOADING:
                                binding.progressLoading.setVisibility(View.VISIBLE);
                                break;
                            case SUCCESS:
                                binding.progressLoading.setVisibility(View.GONE);
                                mostrarPelicula(resource.data);
                                break;
                            case ERROR:
                                binding.progressLoading.setVisibility(View.GONE);
                                mostrarDialogo( "Error", resource.message);
                                break;
                        }
                    }
                });

                viewModel.videos.observe(getViewLifecycleOwner(), resource -> {
                    if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                        configurarTrailer(resource.data);
                    }
                });

            } else {

                viewModel.seleccionarSerie(tmdbIdActual);
                viewModel.serieSeleccionada.observe(getViewLifecycleOwner(), resource -> {
                    if (resource != null) {
                        switch (resource.status) {
                            case LOADING:
                                binding.progressLoading.setVisibility(View.VISIBLE);
                                break;
                            case SUCCESS:
                                binding.progressLoading.setVisibility(View.GONE);
                                mostrarSerie(resource.data);
                                break;
                            case ERROR:
                                binding.progressLoading.setVisibility(View.GONE);
                                mostrarDialogo( "Error", resource.message);
                                break;
                        }
                    }
                });

                viewModel.videos.observe(getViewLifecycleOwner(), resource -> {
                    if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                        configurarTrailer(resource.data);
                    }
                });
            }
        }

        binding.btnVerTrailer.setOnClickListener(v -> {
            if (currentVideoKey != null) {
                abrirYoutube(currentVideoKey);
            } else {
                mostrarDialogo( "Aviso", "Tráiler no disponible");
            }
        });
    }

    private void configurarTrailer(List<Video> videos) {
        boolean encontrado = false;
        for (Video v : videos) {
            if ("YouTube".equals(v.getSite()) && "Trailer".equals(v.getType())) {
                currentVideoKey = v.getKey();
                binding.btnVerTrailer.setEnabled(true);
                binding.btnVerTrailer.setText("VER TRÁILER");
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            binding.btnVerTrailer.setEnabled(false);
            binding.btnVerTrailer.setText("NO DISPONIBLE");
        }
    }

    private void mostrarPelicula(Pelicula p) {
        if (p == null) return;

        binding.tvDetalleTitulo.setText(p.getTitle());
        binding.tvDetalleDescripcion.setText(p.getOverview());
        binding.tvDetalleSubtitulo.setText("Pelicula •");

        boolean soloWifi = prefs.isSoloWifi();
        boolean hayWifi = isWifiConnected(requireContext());

        String imagenPath = p.getBackdropPath() != null ? p.getBackdropPath() : p.getPosterPath();

        if (imagenPath != null) {
            if (soloWifi && !hayWifi) {
                binding.imgDetalleFondo.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                // Carga normal
                Glide.with(this)
                        .load("https://image.tmdb.org/t/p/w780" + imagenPath)
                        .into(binding.imgDetalleFondo);
            }
        }
    }

    private void mostrarSerie(Serie s) {
        if (s == null) return;

        binding.tvDetalleTitulo.setText(s.getName());
        binding.tvDetalleDescripcion.setText(s.getOverview());
        binding.tvDetalleSubtitulo.setText("Serie • " );

        boolean soloWifi = prefs.isSoloWifi();
        boolean hayWifi = isWifiConnected(requireContext());

        String imagenPath = s.getBackdropPath() != null ? s.getBackdropPath() : s.getPosterPath();

        if (imagenPath != null) {
            if (soloWifi && !hayWifi) {
                binding.imgDetalleFondo.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                Glide.with(this)
                        .load("https://image.tmdb.org/t/p/w780" + imagenPath)
                        .into(binding.imgDetalleFondo);
            }
        }
    }

    private void abrirYoutube(String key) {
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key));
            startActivity(webIntent);
        }
    }
    private boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
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