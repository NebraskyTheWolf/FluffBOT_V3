package eu.fluffici.bot.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscordUser {
    private String id;
    private String username;
    private int flags;

    @SerializedName("public_flags")
    private int publicFlags;

    public JsonObject toJSON() {
        JsonObject current = new JsonObject();
        current.addProperty("id",  this.id);
        current.addProperty("username",  this.username);
        current.addProperty("flags",  this.flags);
        current.addProperty("public_flags",  this.publicFlags);

        return current;
    }
}