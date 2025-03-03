package eu.fluffici.furraid.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildAddListener.java

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

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.furraid.FurRaidDB;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static eu.fluffici.bot.api.IconRegistry.ICON_MEDAL;

public class GuildAddListener extends ListenerAdapter {

    private final FurRaidDB instance;

    public GuildAddListener(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void onGuildJoin(@NonNull GuildJoinEvent event) {
        this.instance.getBlacklistManager().createGuild(new GuildSettings(
                event.getGuild().getId(),
                null,
                event.getGuild().getSystemChannel().getId(),
                false,
                null,
                null
        ));

        EmbedBuilder initialMessage = FurRaidDB.getInstance().getEmbed().simpleAuthoredEmbed();
        initialMessage.setAuthor("FurRaidDB Bot", "https://frdb.fluffici.eu", ICON_MEDAL);
        initialMessage.setDescription("""
                👋 **Hello! Thank you for inviting FurRaidDB to your server!**

                FurRaidDB is here to help you keep your server safe and manage it more efficiently with the following features:

                🔒 **Verification** - Ensure only trusted members can access your server.
                🚨 **Anti-Raid Protection** - Automatically detect and block raid attempts.
                🛡️ **Anti-Scam** - Protect your server from scam links and phishing attempts.
                📜 **Moderation Tools** - Kick, ban, and manage users with ease.
                📝 **Customizable Filters** - Set up filters to keep unwanted content out.

                To get started, Use the `/help` command to explore all available features and commands.
                You can also use our dashboard to configure the bot!

                If you need any assistance, feel free to join our [FurRaidDB HQ](https://discord.gg/gAy6AQB8HK) or visit our [Documentation](https://frdbdocs.fluffici.eu).

                **Thank you for choosing FurRaidDB! Let's keep your server safe together!**
                """);

        initialMessage.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        initialMessage.setImage(event.getJDA().getSelfUser().retrieveProfile().complete().getBannerUrl());
        initialMessage.setFooter("FurRaidDB", event.getJDA().getSelfUser().getAvatarUrl());
        initialMessage.setColor(Color.BLUE);

        event.getGuild().getSystemChannel().sendMessageEmbeds(initialMessage.build())
                .setComponents(
                        ActionRow.of(
                                Button.link("https://frdb.fluffici.eu/dashboard", "Dashboard"),
                                Button.link("https://frdb.fluffici.eu", "Website"),
                                Button.link("https://frdbdocs.fluffici.eu", "Documentation")
                        )
                ).queue();
    }
}
