package com.example.proyectoenero;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.proyectoenero.databinding.FragmentDetalleSeguimientoBinding;

import sharedPreferences.PreferencesRepository;
import room.LocalViewModel;

public class DetalleSeguimientoFragment extends Fragment {

    private FragmentDetalleSeguimientoBinding binding;
    private LocalViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetalleSeguimientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferencesRepository prefs = new PreferencesRepository(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        if (getArguments() != null) {
            String idLocal = getArguments().getString("idLocal");

            viewModel.obtenerPorId(idLocal).observe(getViewLifecycleOwner(), item -> {
                if (item != null) {
                    binding.tvTitulo.setText(item.getTitulo());
                    binding.tvFecha.setText("Visto el " + item.getFechaVisualizacion());
                    binding.ratingBar.setRating(item.getPuntuacion());
                    binding.tvPuntuacion.setText(item.getPuntuacion() + "/5");

                    if (item.getDescripcion() != null && !item.getDescripcion().isEmpty()) {
                        binding.tvDescripcion.setText(item.getDescripcion());
                    } else {
                        binding.tvDescripcion.setText("Sin descripción adicional.");
                    }

                    boolean soloWifi = prefs.isSoloWifi();
                    boolean hayWifi = isWifiConnected(requireContext());

                    if (item.getUrlFotoRecuerdo() != null && !item.getUrlFotoRecuerdo().isEmpty()) {
                        Glide.with(this)
                                .load(item.getUrlFotoRecuerdo())
                                .into(binding.imgDetalle);
                    } else if (item.getPosterPath() != null) {
                        if (soloWifi && !hayWifi) {
                            binding.imgDetalle.setImageResource(android.R.drawable.ic_menu_gallery);
                        } else {
                            Glide.with(this)
                                    .load("https://image.tmdb.org/t/p/w780" + item.getPosterPath())
                                    .into(binding.imgDetalle);
                        }
                    }
                }
            });
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
}