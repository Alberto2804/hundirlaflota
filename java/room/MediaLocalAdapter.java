package room;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectoenero.R;

import java.util.ArrayList;
import java.util.List;

import sharedPreferences.PreferencesRepository;

public class MediaLocalAdapter extends RecyclerView.Adapter<MediaLocalAdapter.ViewHolder> {

    private List<MediaEntity> lista = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(MediaEntity media);
        void onItemClick(MediaEntity media);
    }

    public MediaLocalAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLista(List<MediaEntity> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_pelicula, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaEntity item = lista.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitulo.setText(item.getTitulo());
        holder.tvDescripcion.setText(item.getDescripcion());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        PreferencesRepository prefs = new PreferencesRepository(context);
        boolean soloWifi = prefs.isSoloWifi();
        boolean hayWifi = isWifiConnected(context);

        if (item.getUrlFotoRecuerdo() != null && !item.getUrlFotoRecuerdo().isEmpty()) {
            Glide.with(context)
                    .load(item.getUrlFotoRecuerdo())
                    .into(holder.imgPoster);
            holder.imgPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else if (item.getPosterPath() != null) {
            if (soloWifi && !hayWifi) {
                holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500" + item.getPosterPath())
                        .into(holder.imgPoster);
            }
        } else {
            holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (item.isEsPendiente()) {
            holder.tvSubtitulo.setText("Pendiente • " + item.getTipo());
        } else {
            holder.tvSubtitulo.setText("Visto el " + item.getFechaVisualizacion() + " • ★ " + item.getPuntuacion());
        }

        holder.btnAccion.setImageResource(android.R.drawable.ic_menu_delete);
        holder.btnAccion.setOnClickListener(v -> listener.onDeleteClick(item));

        if ("SERIE".equals(item.getTipo())) {
            holder.icMedia.setImageResource(android.R.drawable.ic_menu_agenda);
        } else {
            holder.icMedia.setImageResource(android.R.drawable.ic_menu_slideshow);
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    private boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster, btnAccion, icMedia;
        TextView tvTitulo, tvSubtitulo, tvDescripcion;

        ViewHolder(View v) {
            super(v);
            imgPoster = v.findViewById(R.id.imgPoster);
            btnAccion = v.findViewById(R.id.btnAgregarPendiente);
            icMedia = v.findViewById(R.id.icMedia);

            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvSubtitulo = v.findViewById(R.id.tvSubtitulo);
            tvDescripcion = v.findViewById(R.id.tvDescripcion);
        }
    }
}