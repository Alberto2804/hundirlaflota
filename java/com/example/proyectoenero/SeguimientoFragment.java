package com.example.proyectoenero;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyectoenero.databinding.FragmentSeguimientoBinding;
import com.google.firebase.firestore.Query;

import java.util.List;

import api.Resource;
import room.LocalViewModel;
import room.MediaEntity;
import room.MediaLocalAdapter;

public class SeguimientoFragment extends Fragment {

    private FragmentSeguimientoBinding binding;
    private LocalViewModel viewModel;
    private MediaLocalAdapter adapter;

    // Variables globales para guardar el estado de los filtros
    private String filtroTitulo = null;
    private Float filtroMinPunt = null;
    private Float filtroMaxPunt = null;
    private String campoOrden = null; // Ordenar por fecha por defecto
    private Query.Direction direccionOrden = null; // Más reciente primero

    private Long filtroFechaDesde = null;
    private Long filtroFechaHasta = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSeguimientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(LocalViewModel.class);

        // Configurar RecyclerView
        adapter = new MediaLocalAdapter(new MediaLocalAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(MediaEntity item) {
                viewModel.eliminarSeguimiento(item);
            }

            @Override
            public void onItemClick(MediaEntity item) {
                Bundle bundle = new Bundle();
                bundle.putString("idLocal", item.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.detalleSeguimientoFragment, bundle);
            }
        });
        binding.recyclerSeguimiento.setAdapter(adapter);
        binding.recyclerSeguimiento.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_global_anadirSeguimiento)
        );

        binding.btnFiltros.setOnClickListener(v -> mostrarDialogoFiltros());
        binding.btnOrdenar.setOnClickListener(v -> mostrarDialogoOrdenacion());

        // 1. Configuramos el SearchView como en el tutorial
        configurarSearchView();

        // 2. Primera carga de datos
        aplicarFiltrosYBuscar();
    }

    // Igual al tutorial de Pokémon: detecta cuando el usuario le da a "Buscar" en el teclado
    private void configurarSearchView() {
        binding.searchViewTitulo.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtroTitulo = query;
                aplicarFiltrosYBuscar();
                binding.searchViewTitulo.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Si el usuario borra todo el texto, restauramos la lista completa automáticamente
                if (newText.isEmpty()) {
                    filtroTitulo = null;
                    aplicarFiltrosYBuscar();
                }
                return false;
            }
        });
    }

    // Método principal de búsqueda
    private void aplicarFiltrosYBuscar() {
        viewModel.obtenerSeguimientosFiltrados(
                filtroTitulo, filtroMinPunt, filtroMaxPunt,
                filtroFechaDesde, filtroFechaHasta,
                campoOrden, direccionOrden
        ).observe(getViewLifecycleOwner(), resource -> {

            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    // Mostrar spinner
                    binding.progressLoading.setVisibility(View.VISIBLE);
                    // Ocultar el contenido y el error
                    binding.recyclerSeguimiento.setVisibility(View.GONE);
                    binding.layoutError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.progressLoading.setVisibility(View.GONE);
                    List<MediaEntity> lista = resource.data;

                    if (lista == null || lista.isEmpty()) {
                        // Si no hay datos, mostramos la pantalla de error/vacío
                        binding.recyclerSeguimiento.setVisibility(View.GONE);
                        binding.layoutError.setVisibility(View.VISIBLE);
                        binding.tvErrorMessage.setText("No se encontraron resultados");
                    } else {
                        // Mostrar contenido normal
                        binding.layoutError.setVisibility(View.GONE);
                        binding.recyclerSeguimiento.setVisibility(View.VISIBLE);
                        adapter.setLista(lista);
                    }
                    break;

                case ERROR:
                    // Ocultar spinner y contenido
                    binding.progressLoading.setVisibility(View.GONE);
                    binding.recyclerSeguimiento.setVisibility(View.GONE);
                    // Mostrar layout de error real
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText("Error: " + resource.message);
                    break;
            }
        });
    }

    // MENÚ DE ORDENACIÓN (Con opción para resetear)
    private void mostrarDialogoOrdenacion() {
        String[] opciones = {"Más reciente", "Menos reciente", "Puntuación (Mayor a menor)", "Puntuación (Menor a mayor)", "❌ Quitar ordenación"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Ordenar por")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: campoOrden = "fechaMillis"; direccionOrden = Query.Direction.DESCENDING; break;
                        case 1: campoOrden = "fechaMillis"; direccionOrden = Query.Direction.ASCENDING; break;
                        case 2: campoOrden = "puntuacion"; direccionOrden = Query.Direction.DESCENDING; break;
                        case 3: campoOrden = "puntuacion"; direccionOrden = Query.Direction.ASCENDING; break;
                        case 4: campoOrden = null; direccionOrden = null; break; // Restablecer
                    }
                    aplicarFiltrosYBuscar();
                }).show();
    }


    private void mostrarDialogoFiltros() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filtros, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Referencias a los componentes del XML
        TextView tvFechaDesde = dialogView.findViewById(R.id.tvFechaDesde);
        TextView tvFechaHasta = dialogView.findViewById(R.id.tvFechaHasta);
        Spinner spinnerMin = dialogView.findViewById(R.id.spinnerMin);
        Spinner spinnerMax = dialogView.findViewById(R.id.spinnerMax);
        View btnAplicar = dialogView.findViewById(R.id.btnAplicarFiltros);
        View btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFiltros);

        // Variables temporales para no machacar las globales hasta que le dé a "Aplicar"
        final Long[] tempFechaDesde = {filtroFechaDesde};
        final Long[] tempFechaHasta = {filtroFechaHasta};

        // Rellenar Spinners (De 0 a 5)
        String[] puntuaciones = {"0", "1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapterSpinners = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, puntuaciones);
        spinnerMin.setAdapter(adapterSpinners);
        spinnerMax.setAdapter(adapterSpinners);

        // Si ya había filtros puestos, los dejamos seleccionados
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (filtroFechaDesde != null) {
            tvFechaDesde.setText(sdf.format(filtroFechaDesde));
            tvFechaDesde.setTextColor(getResources().getColor(android.R.color.black));
        }
        if (filtroFechaHasta != null) {
            tvFechaHasta.setText(sdf.format(filtroFechaHasta));
            tvFechaHasta.setTextColor(getResources().getColor(android.R.color.black));
        }
        if (filtroMinPunt != null) spinnerMin.setSelection(Math.round(filtroMinPunt));
        if (filtroMaxPunt != null) spinnerMax.setSelection(Math.round(filtroMaxPunt));
        else spinnerMax.setSelection(5); // Por defecto el máximo en 5

        // Eventos de los Calendarios
        tvFechaDesde.setOnClickListener(v -> abrirCalendario(tvFechaDesde, tempFechaDesde));
        tvFechaHasta.setOnClickListener(v -> abrirCalendario(tvFechaHasta, tempFechaHasta));

        // Aplicar Filtros con VALIDACIÓN
        btnAplicar.setOnClickListener(v -> {
            float minSel = Float.parseFloat(spinnerMin.getSelectedItem().toString());
            float maxSel = Float.parseFloat(spinnerMax.getSelectedItem().toString());

            // 1. Validar Puntuaciones
            if (minSel > maxSel) {
                mostrarDialogo( "Error de filtro", "El mínimo no puede ser mayor que el máximo");
                return;
            }

            // 2. Validar Fechas
            if (tempFechaDesde[0] != null && tempFechaHasta[0] != null && tempFechaDesde[0] > tempFechaHasta[0]) {
                mostrarDialogo( "Error de filtro", "La fecha 'Desde' no puede ser posterior a 'Hasta'");
                return;
            }

            // Si todo es correcto, guardamos y buscamos
            filtroMinPunt = minSel;
            filtroMaxPunt = maxSel;
            filtroFechaDesde = tempFechaDesde[0];
            filtroFechaHasta = tempFechaHasta[0];

            aplicarFiltrosYBuscar();
            dialog.dismiss();
        });

        // Limpiar Filtros
        btnLimpiar.setOnClickListener(v -> {
            filtroMinPunt = null;
            filtroMaxPunt = null;
            filtroFechaDesde = null;
            filtroFechaHasta = null;

            aplicarFiltrosYBuscar();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void abrirCalendario(TextView textView, Long[] storageVar) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0); // Fijar a las 00:00:00

                    storageVar[0] = selected.getTimeInMillis();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    textView.setText(sdf.format(selected.getTime()));
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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