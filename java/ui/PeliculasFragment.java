package ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoenero.databinding.FragmentPeliculasBinding;

import room.LocalViewModel;
import viewmodel.TmdbViewModel;

public class PeliculasFragment extends Fragment {

    private FragmentPeliculasBinding binding;
    private TmdbViewModel viewModel;
    private LocalViewModel localViewModel;
    private PeliculasAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPeliculasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);
        localViewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        configurarRecyclerView();

        observarPeliculas();

        configurarPaginacion();

        if (adapter.getItemCount() == 0) {
            viewModel.cargarPeliculas();
        }
    }

    private void configurarRecyclerView() {
        // 2. Pasamos el localViewModel al Adapter para que funcione el botón (+)
        adapter = new PeliculasAdapter(requireContext(), viewModel, localViewModel);

        binding.rvPeliculas.setAdapter(adapter);
        binding.rvPeliculas.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observarPeliculas() {
        viewModel.listaPeliculas.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    if (adapter.getItemCount() == 0) {
                        binding.progressLoading.setVisibility(View.VISIBLE);
                        binding.rvPeliculas.setVisibility(View.GONE);
                    }
                    binding.layoutError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressLoading.setVisibility(View.GONE);
                    binding.layoutError.setVisibility(View.GONE);
                    binding.rvPeliculas.setVisibility(View.VISIBLE);

                    if (resource.data != null) {
                        adapter.setPeliculasList(resource.data);
                    }
                    break;

                case ERROR:
                    binding.progressLoading.setVisibility(View.GONE);
                    if (adapter.getItemCount() == 0) {
                        binding.rvPeliculas.setVisibility(View.GONE);
                        binding.layoutError.setVisibility(View.VISIBLE);
                        binding.tvError.setText(resource.message);
                    }
                    break;
            }
        });
    }

    private void configurarPaginacion() {
        binding.rvPeliculas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    viewModel.cargarPeliculas();
                }
            }
        });
    }
}