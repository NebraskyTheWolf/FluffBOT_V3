package eu.fluffici.bot.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildUserLeftListener.java

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
import eu.fluffici.bot.api.beans.statistics.GuildEngagement;
import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_USER_X;

@SuppressWarnings("All")
public class GuildUserLeftListener extends ListenerAdapter {

    private final FluffBOT instance;

    public GuildUserLeftListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    @SneakyThrows
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;

        event.getGuild().getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.goodbye")).sendMessageEmbeds(this.instance.getEmbed().simpleAuthoredEmbed()
                .setAuthor(String.format("%s se odpojil/a", event.getMember().getUser().getEffectiveName()), "https://fluffici.eu", ICON_USER_X.getUrl())
                .setColor(Color.RED)
                .setFooter(event.getMember().getId())
                .setThumbnail(event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build()).queue();

        this.instance.getGameServiceManager().insertGuildEngagement(event.getMember(), GuildEngagement.Action.GUILD_LEAVE);
        FluffBOT.getInstance().getGameServiceManager().removeReminder(event.getUser());

        TicketBuilder ticket = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchTicketByUser(event.getUser());

        if (ticket != null && ticket.getStatus() == "OPENED") {
            event.getGuild().getTextChannelById(ticket.getChannelId()).sendMessage(
                    """
                    **%s** left the server.
                    """.formatted(event.getMember().getEffectiveName())
            ).queue();
        }
    }
}
