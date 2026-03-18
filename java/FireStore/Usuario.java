package FireStore;

public class Usuario {
    private String displayName;
    private String email;
    private long createdAt;

    private String fotoPerfilUrl;

    public Usuario() {}

    public Usuario(String displayName, String email, long createdAt) {
        this.displayName = displayName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
