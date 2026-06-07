package be.volley.live.model;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes TeamColor as its hex string in JSON API responses,
 * so the dashboard and scorer JS can use it directly as a CSS color.
 */
public class TeamColorSerializer extends JsonSerializer<TeamColor> {
    @Override
    public void serialize(TeamColor value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value != null ? value.getHex() : "#ffffff");
    }
}
