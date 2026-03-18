package room;

public class MediaEntity {
    private String id;

    private int tmdbId;
    private String titulo;
    private String descripcion;
    private String posterPath;
    private String tipo;
    private boolean esPendiente;
    private boolean esSeguimiento;

    private String fechaVisualizacion;
    private float puntuacion;
    private String urlFotoRecuerdo;

    public String userId;

    private long fechaMillis;


    public MediaEntity() {}

    public long getFechaMillis() { return fechaMillis; }
    public void setFechaMillis(long fechaMillis) { this.fechaMillis = fechaMillis; }

    public String getUrlFotoRecuerdo() { return urlFotoRecuerdo; }
    public void setUrlFotoRecuerdo(String urlFotoRecuerdo) { this.urlFotoRecuerdo = urlFotoRecuerdo; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isEsPendiente() { return esPendiente; }
    public void setEsPendiente(boolean esPendiente) { this.esPendiente = esPendiente; }
    public boolean isEsSeguimiento() { return esSeguimiento; }
    public void setEsSeguimiento(boolean esSeguimiento) { this.esSeguimiento = esSeguimiento; }
    public String getFechaVisualizacion() { return fechaVisualizacion; }
    public void setFechaVisualizacion(String fechaVisualizacion) { this.fechaVisualizacion = fechaVisualizacion; }
    public float getPuntuacion() { return puntuacion; }
    public void setPuntuacion(float puntuacion) { this.puntuacion = puntuacion; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}