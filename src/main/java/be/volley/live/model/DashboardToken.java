package be.volley.live.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dashboard_tokens")
public class DashboardToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token; // random string included in dashboard URL

    private String label; // e.g. "Gym screen 1"

    private String tenantId;

    public DashboardToken() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
