package com.example.proyectoenero;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.proyectoenero.databinding.FragmentAnadirSeguimientoBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import api.Resource;
import room.ImageUtils;
import data.Pelicula;
import data.Serie;
import room.MediaEntity;
import room.LocalViewModel;
import viewmodel.TmdbViewModel;

public class AnadirSeguimientoFragment extends Fragment {

    private FragmentAnadirSeguimientoBinding binding;
    private LocalViewModel localViewModel;
    private TmdbViewModel tmdbViewModel;
    private List<MediaEntity> listaResultadosTemp = new ArrayList<>();
    private MediaEntity seleccionadoEnSpinner = null;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.imgPreview.setImageURI(selectedImageUri);
                    binding.imgPreview.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnadirSeguimientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localViewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);
        tmdbViewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);

        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());

        binding.btnFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnBuscarApi.setOnClickListener(v -> realizarBusqueda());

        observarResultadosBusqueda();
        binding.btnGuardar.setOnClickListener(v -> guardarEnBaseDeDatos(v));


        if (getArguments() != null) {
            String tituloPre = getArguments().getString("tituloPre");
            String tipoPre = getArguments().getString("tipoPre");

            if (tituloPre != null) {
                binding.etBusqueda.setText(tituloPre);

            }

            if (tipoPre != null) {
                if ("PELICULA".equalsIgnoreCase(tipoPre)) {
                    binding.rbPelicula.setChecked(true);
                } else if ("SERIE".equalsIgnoreCase(tipoPre)) {
                    binding.rbSerie.setChecked(true);
                }
            }
        }
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fecha = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    binding.etFecha.setText(fecha);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void realizarBusqueda() {
        String query = binding.etBusqueda.getText().toString().trim();
        if (query.isEmpty()) {
            binding.etBusqueda.setError("Escribe algo para buscar");
            return;
        }

        if (binding.rbPelicula.isChecked()) {
            tmdbViewModel.buscarPeliculas(query);
        } else {
            tmdbViewModel.buscarSeries(query);
        }

        mostrarDialogo( "Buscando", "Iniciando búsqueda...");
    }

    private void observarResultadosBusqueda() {
        tmdbViewModel.resultadosBusquedaPeliculas.observe(getViewLifecycleOwner(), resource -> {
            if (binding.rbPelicula.isChecked() && resource.status == Resource.Status.SUCCESS) {
                actualizarSpinnerPeliculas(resource.data);
            }
        });

        tmdbViewModel.resultadosBusquedaSeries.observe(getViewLifecycleOwner(), resource -> {
            if (binding.rbSerie.isChecked() && resource.status == Resource.Status.SUCCESS) {
                actualizarSpinnerSeries(resource.data);
            }
        });
    }

    private void actualizarSpinnerPeliculas(List<Pelicula> peliculas) {
        listaResultadosTemp.clear();
        List<String> titulos = new ArrayList<>();

        if (peliculas == null || peliculas.isEmpty()) {
            titulos.add("Sin resultados");
        } else {
            for (Pelicula p : peliculas) {
                titulos.add(p.getTitle());
                MediaEntity m = new MediaEntity();
                m.setTmdbId(p.getId());
                m.setTitulo(p.getTitle());
                m.setDescripcion(p.getOverview());
                m.setPosterPath(p.getPosterPath());
                m.setTipo("PELICULA");
                listaResultadosTemp.add(m);
            }
        }
        configurarSpinner(titulos);
    }

    private void actualizarSpinnerSeries(List<Serie> series) {
        listaResultadosTemp.clear();
        List<String> titulos = new ArrayList<>();

        if (series == null || series.isEmpty()) {
            titulos.add("Sin resultados");
        } else {
            for (Serie s : series) {
                titulos.add(s.getName());
                MediaEntity m = new MediaEntity();
                m.setTmdbId(s.getId());
                m.setTitulo(s.getName());
                m.setDescripcion(s.getOverview());
                m.setPosterPath(s.getPosterPath());
                m.setTipo("SERIE");
                listaResultadosTemp.add(m);
            }
        }
        configurarSpinner(titulos);
    }

    private void configurarSpinner(List<String> titulos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, titulos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerResultados.setAdapter(adapter);
    }

    private void guardarEnBaseDeDatos(View v) {
        int pos = binding.spinnerResultados.getSelectedItemPosition();
        if (pos < 0 || pos >= listaResultadosTemp.size()) {
            mostrarDialogo( "Aviso", "Primero busca y selecciona un contenido");
            return;
        }

        seleccionadoEnSpinner = listaResultadosTemp.get(pos);
        String fecha = binding.etFecha.getText().toString();
        if (fecha.isEmpty()) {
            binding.etFecha.setError("Selecciona una fecha");
            return;
        }

        seleccionadoEnSpinner.setFechaVisualizacion(fecha);
        seleccionadoEnSpinner.setPuntuacion(binding.ratingBar.getRating());
        seleccionadoEnSpinner.setEsSeguimiento(true);
        seleccionadoEnSpinner.setEsPendiente(false);

        seleccionadoEnSpinner.setFechaMillis(System.currentTimeMillis());

        // FASE 1: Subir a Supabase (si hay foto)
        if (selectedImageUri != null) {
            localViewModel.uploadImage(selectedImageUri).observe(getViewLifecycleOwner(), uploadRes -> {
                switch (uploadRes.status) {
                    case LOADING:
                        mostrarDialogo( "Información", "Subiendo imagen...");
                        break;
                    case SUCCESS:
                        guardarEnFirestore(v, uploadRes.data);
                        break;
                    case ERROR:
                        mostrarDialogo( "Error", "Error imagen: " + uploadRes.message);
                        break;
                }
            });
        } else {
            guardarEnFirestore(v, null);
        }
    }

    private void guardarEnFirestore(View v, String urlImagen) {
        seleccionadoEnSpinner.setUrlFotoRecuerdo(urlImagen);

        localViewModel.insertarSeguimientoUnico(seleccionadoEnSpinner).observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case SUCCESS:
                    mostrarDialogo( "Éxito", "Guardado correctamente");
                    Navigation.findNavController(v).popBackStack();
                    break;
                case ERROR:
                    mostrarDialogo( "Error", result.message);
                    break;
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