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

import com.example.proyectoenero.databinding.FragmentSerieBinding;

import room.LocalViewModel;
import viewmodel.TmdbViewModel;

public class SerieFragment extends Fragment {

    private FragmentSerieBinding binding;
    private TmdbViewModel viewModel;
    private LocalViewModel localViewModel;
    private SeriesAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSerieBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);
        localViewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        configurarRecyclerView();
        observarSeries();
        configurarPaginacion();

        if (adapter.getItemCount() == 0) {
            viewModel.cargarSeries();
        }
    }

    private void configurarRecyclerView() {
        adapter = new SeriesAdapter(requireContext(), viewModel, localViewModel);

        binding.recyclerSeries.setAdapter(adapter);
        binding.recyclerSeries.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observarSeries() {
        viewModel.listaSeries.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    if (adapter.getItemCount() == 0) {
                        binding.progressLoading.setVisibility(View.VISIBLE);
                        binding.recyclerSeries.setVisibility(View.GONE);
                    }
                    binding.layoutError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressLoading.setVisibility(View.GONE);
                    binding.layoutError.setVisibility(View.GONE);
                    binding.recyclerSeries.setVisibility(View.VISIBLE);

                    if (resource.data != null) {
                        adapter.setSeriesList(resource.data);
                    }
                    break;

                case ERROR:
                    binding.progressLoading.setVisibility(View.GONE);
                    if (adapter.getItemCount() == 0) {
                        binding.recyclerSeries.setVisibility(View.GONE);
                        binding.layoutError.setVisibility(View.VISIBLE);
                        binding.tvError.setText(resource.message);
                    }
                    break;
            }
        });
    }

    private void configurarPaginacion() {
        binding.recyclerSeries.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    viewModel.cargarSeries();
                }
            }
        });
    }
}