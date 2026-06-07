package be.volley.live.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tenants")
public class Tenant {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug; // e.g. "wavoc"

    private String name; // e.g. "WAVOC Volleyball"

    public Tenant() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
