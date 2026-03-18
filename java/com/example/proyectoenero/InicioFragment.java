package com.example.proyectoenero;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.proyectoenero.databinding.FragmentInicioBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.Random;


import api.Resource;
import room.LocalViewModel;
import room.MediaEntity;
import sharedPreferences.PreferencesRepository;
import ui.PeliculasFragment;
import ui.SerieFragment;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private PreferencesRepository prefs;
    private LocalViewModel localViewModel;


    private boolean dialogoMostrado = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Películas");
            } else {
                tab.setText("Series");
            }
        }).attach();

        prefs = new PreferencesRepository(requireContext());
        localViewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        NavController navController = Navigation.findNavController(view);

        if (!dialogoMostrado) {
            verificarEstadoUsuario(navController);
        }
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new PeliculasFragment();
            } else {
                return new SerieFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    private void verificarEstadoUsuario(NavController navController) {
        String nombre = prefs.getNombreUsuario();

        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarDialogoSinNombre(navController);
            dialogoMostrado = true;
        }

        else {
            localViewModel.obtenerPendientes().observe(getViewLifecycleOwner(), resource -> {
                if (!dialogoMostrado && resource != null && resource.status == Resource.Status.SUCCESS) {
                    List<MediaEntity> lista = resource.data;

                    if (lista != null && !lista.isEmpty()) {
                        MediaEntity pendienteAleatorio = obtenerPendienteAleatorio(lista);
                        mostrarDialogoPendiente(navController, nombre, pendienteAleatorio);
                        dialogoMostrado = true;
                    }
                }
            });
        }
    }

    private MediaEntity obtenerPendienteAleatorio(List<MediaEntity> lista) {
        Random random = new Random();
        int index = random.nextInt(lista.size());
        return lista.get(index);
    }

    private void mostrarDialogoPendiente(NavController navController, String nombre, MediaEntity media) {
        new AlertDialog.Builder(requireContext())
                .setTitle("¡Hola, " + nombre + "!")
                .setMessage("¿Has visto ya tu pendiente " + media.getTitulo() + "?")
                .setCancelable(false)
                .setPositiveButton("Ir a Seguimiento", (dialog, which) -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("tituloPre", media.getTitulo());
                    bundle.putString("tipoPre", media.getTipo());

                    try {
                        navController.navigate(R.id.action_global_anadirSeguimiento, bundle);
                    } catch (Exception e) {
                    }
                })
                .setNegativeButton("Aún no", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void mostrarDialogoSinNombre(NavController navController) {
        new AlertDialog.Builder(requireContext())
                .setTitle("¡Bienvenido!")
                .setMessage("¡Hola! Para personalizar tu experiencia, puedes configurar tu nombre en los ajustes. ¿Quieres hacerlo ahora?")
                .setCancelable(false)
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    navController.navigate(R.id.ajustesFragment);
                })
                .setNegativeButton("Más tarde", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}