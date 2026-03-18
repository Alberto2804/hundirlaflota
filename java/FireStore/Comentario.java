package FireStore;

public class Comentario {
    private String authorUid;
    private String authorName;
    private String text;
    private long createdAt;

    public Comentario() {}

    public Comentario(String authorUid, String authorName, String text, long createdAt) {
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = createdAt;
    }
    public String getAuthorUid() { return authorUid; }
    public void setAuthorUid(String authorUid) { this.authorUid = authorUid; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
