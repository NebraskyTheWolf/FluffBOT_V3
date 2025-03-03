package eu.fluffici.bot.database.request;

/*
---------------------------------------------------------------------------------
File Name : RequestSendEvent.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/



import com.google.gson.JsonObject;
import com.runarmc.annotations.Route;
import com.runarmc.models.AbstractPostModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.ScheduledEvent;

@Getter
@AllArgsConstructor
@Route(
        method = "POST",
        route = "/api/discord/schedule-event/create",
        result = RequestSendEvent.class
)
public class RequestSendEvent extends AbstractPostModel {

    private ScheduledEvent scheduled;

    @Override
    public JsonObject body() {
        JsonObject body = new JsonObject();

        body.addProperty("guildId", this.scheduled.getGuild().getId());
        body.addProperty("creatorId", this.scheduled.getCreatorId());
        body.addProperty("eventId", this.scheduled.getId());
        body.addProperty("eventName", this.scheduled.getName());
        body.addProperty("eventDescription", this.scheduled.getDescription());
        body.addProperty("eventBanner", (this.scheduled.getImageUrl() == null ? "" : this.scheduled.getImageUrl()));
        body.addProperty("eventLocation", this.scheduled.getLocation());
        body.addProperty("eventType", this.scheduled.getType().name());
        body.addProperty("eventStatus", this.scheduled.getStatus().name());
        body.addProperty("eventStartTime", this.scheduled.getStartTime().toEpochSecond());

        if (this.scheduled.getEndTime() == null) {
            body.addProperty("eventEndTime",  this.scheduled.getStartTime().plusHours(2).toEpochSecond());
        } else {
            body.addProperty("eventEndTime",  this.scheduled.getEndTime().toEpochSecond());
        }

        body.addProperty("eventLink", this.scheduled.getEndTime().toEpochSecond());

        return body;
    }
}