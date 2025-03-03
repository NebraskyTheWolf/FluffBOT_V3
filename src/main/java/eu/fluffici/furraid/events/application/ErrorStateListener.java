package eu.fluffici.furraid.events.application;

/*
---------------------------------------------------------------------------------
File Name : ErrorStateListener.java

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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.furraid.FurRaidDB;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ErrorStateListener extends ListenerAdapter {
    private long lastMs = 0L;

    @Override
    @SneakyThrows
    public void onSessionDisconnect(@NonNull SessionDisconnectEvent event) {
        FurRaidDB.getInstance().getLogger().info("WebSocket disconnected...");
    }

    @Override
    public void onGatewayPing(@NotNull GatewayPingEvent event) {
        long oldPing = event.getOldPing();
        long newPing = event.getNewPing();

        FurRaidDB.getInstance().getLogger().debug("%s ping diff (last ping: %s)", Math.abs(oldPing - newPing), this.lastMs);
        this.lastMs = Math.abs(oldPing - newPing);
    }
}
