package data;

import java.util.List;

public class PagedResponse<T> {
    private int page;
    private List<T> results;

    public int getPage() { return page; }
    public List<T> getResults() { return results; }
}
