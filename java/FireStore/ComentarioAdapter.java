package FireStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyectoenero.R;
import java.util.ArrayList;
import java.util.List;
import FireStore.Comentario;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private List<Comentario> lista = new ArrayList<>();

    public void setLista(List<Comentario> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario c = lista.get(position);

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(c.getCreatedAt());

        holder.tvAutor.setText(c.getAuthorName());
        holder.tvFecha.setText(timeAgo);
        holder.tvTexto.setText(c.getText());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAutor, tvFecha, tvTexto;

        ViewHolder(View v) {
            super(v);
            tvAutor = v.findViewById(R.id.tvAutor);
            tvFecha = v.findViewById(R.id.tvFecha);
            tvTexto = v.findViewById(R.id.tvTexto);
        }
    }
}
