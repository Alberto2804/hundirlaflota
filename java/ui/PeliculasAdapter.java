package ui;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.proyectoenero.R;
import java.util.ArrayList;
import java.util.List;
import data.Pelicula;
import room.MediaEntity;
import room.LocalViewModel;
import viewmodel.TmdbViewModel;
import sharedPreferences.PreferencesRepository;

public class PeliculasAdapter extends RecyclerView.Adapter<PeliculasAdapter.PeliculaViewHolder> {

    private List<Pelicula> peliculasList;
    private TmdbViewModel tmdbViewModel;
    private LocalViewModel localViewModel;
    private final LayoutInflater inflater;
    private final Context context;
    private final PreferencesRepository prefs;

    public PeliculasAdapter(Context context, TmdbViewModel tmdbViewModel, LocalViewModel localViewModel) {
        this.context = context;
        this.tmdbViewModel = tmdbViewModel;
        this.localViewModel = localViewModel;
        this.inflater = LayoutInflater.from(context);
        this.peliculasList = new ArrayList<>();
        this.prefs = new PreferencesRepository(context); // Inicializamos prefs
    }

    @NonNull
    @Override
    public PeliculaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.viewholder_pelicula, parent, false);
        return new PeliculaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PeliculaViewHolder holder, int position) {
        Pelicula pelicula = peliculasList.get(position);

        holder.tvTitulo.setText(pelicula.getTitle());
        holder.tvSubtitulo.setText("Película • Popular");
        holder.tvDescripcion.setText(pelicula.getOverview());

        boolean soloWifi = prefs.isSoloWifi();
        boolean hayWifi = isWifiConnected(context);

        if (soloWifi && !hayWifi) {
            holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500" + pelicula.getPosterPath())
                    .into(holder.imgPoster);
        }

        holder.itemView.setOnClickListener(v -> {
            tmdbViewModel.seleccionarPelicula(pelicula.getId());
            Bundle bundle = new Bundle();
            bundle.putInt("id", pelicula.getId());
            bundle.putBoolean("esPelicula", true);
            Navigation.findNavController(v).navigate(R.id.detalleFragment, bundle);
        });

        holder.btnAgregarPendiente.setOnClickListener(v -> {
            MediaEntity media = new MediaEntity();
            media.setTmdbId(pelicula.getId());
            media.setTitulo(pelicula.getTitle());
            media.setDescripcion(pelicula.getOverview());
            media.setPosterPath(pelicula.getPosterPath());
            media.setTipo("PELICULA");
            media.setEsPendiente(true);
            media.setEsSeguimiento(false);

            localViewModel.insertarPendienteUnico(media, exito -> {
                v.post(() -> {
                    if (exito) {
                        Toast.makeText(context, "Añadido a pendientes", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "¡Ya está en tu lista!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    @Override
    public int getItemCount() { return peliculasList != null ? peliculasList.size() : 0; }

    public void setPeliculasList(List<Pelicula> list) {
        this.peliculasList = list;
        notifyDataSetChanged();
    }

    public void addPeliculasList(List<Pelicula> list) {
        int start = peliculasList.size();
        this.peliculasList.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }


    private boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        }
        return false;
    }

    class PeliculaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster, btnAgregarPendiente;
        TextView tvTitulo, tvSubtitulo, tvDescripcion;

        public PeliculaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            btnAgregarPendiente = itemView.findViewById(R.id.btnAgregarPendiente);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvSubtitulo = itemView.findViewById(R.id.tvSubtitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }


}