package com.example.proyectoenero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyectoenero.R;
import com.example.proyectoenero.databinding.FragmentPendientesBinding;

import java.util.List;
import room.MediaEntity;
import room.LocalViewModel;
import room.MediaLocalAdapter;

public class PendientesFragment extends Fragment {

    private FragmentPendientesBinding binding;
    private LocalViewModel viewModel;
    private MediaLocalAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPendientesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        adapter = new MediaLocalAdapter(new MediaLocalAdapter.OnItemClickListener() {

            @Override
            public void onDeleteClick(MediaEntity item) {
                viewModel.eliminarPendiente(item);
            }

            @Override
            public void onItemClick(MediaEntity item) {
                Bundle bundle = new Bundle();
                bundle.putInt("id", item.getTmdbId());

                boolean esPelicula = "PELICULA".equalsIgnoreCase(item.getTipo());
                bundle.putBoolean("esPelicula", esPelicula);

                Navigation.findNavController(view).navigate(R.id.detalleFragment, bundle);
            }
        });

        binding.recyclerPendientes.setAdapter(adapter);
        binding.recyclerPendientes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.progressLoading.setVisibility(View.GONE);

        viewModel.obtenerPendientes().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressLoading.setVisibility(View.VISIBLE);
                        binding.recyclerPendientes.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressLoading.setVisibility(View.GONE);
                        if (resource.data != null) {
                            adapter.setLista(resource.data);
                            actualizarVistaVacia(resource.data);
                        }
                        break;
                    case ERROR:
                        binding.progressLoading.setVisibility(View.GONE);
                        mostrarDialogo( "Información", resource.message);
                        break;
                }
            }
        });

        binding.btnIrExplorar.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.inicioFragment);
        });
    }

    private void actualizarVistaVacia(List<MediaEntity> lista) {
        if (lista == null || lista.isEmpty()) {
            binding.recyclerPendientes.setVisibility(View.GONE);
            binding.layoutVacio.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerPendientes.setVisibility(View.VISIBLE);
            binding.layoutVacio.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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