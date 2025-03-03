package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : InteractionBuilder.java

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


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.sql.Timestamp;
@Getter
@Setter
@Builder
public class InteractionBuilder {
    private ButtonStyle style;
    private String buttonName;
    private String buttonId;

    private String interactionId, userId;
    private String customId, messageId, channelId;
    private Boolean isExpired, isAcknowledged, isAttached, isUpdated, isDm;
    private Timestamp expiration, createdAt, updatedAt;

    public Interactions toInteraction() {
        return new Interactions(
                this.interactionId,
                this.userId,
                this.customId,
                this.messageId,
                this.channelId,
                this.isAttached,
                this.isAcknowledged,
                this.isExpired,
                this.isUpdated,
                this.isDm,
                this.expiration,
                this.createdAt,
                this.updatedAt
        );
    }
}
