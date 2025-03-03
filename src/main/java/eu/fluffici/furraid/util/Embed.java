package eu.fluffici.furraid.util;

/*
---------------------------------------------------------------------------------
File Name : Embed.java

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


import eu.fluffici.bot.api.hooks.IEmbed;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Embed implements IEmbed {
    private final FurRaidDB instance;

    public Embed(FurRaidDB fluffbot)  {
        this.instance = fluffbot;
    }
    @Override
    public EmbedBuilder simpleFieldedEmbed(String title, String description, Color color, List<MessageEmbed.Field> fields) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#2F3136"))
                .setTimestamp(Instant.now())
                .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
        fields.forEach(embedBuilder::addField);
        return embedBuilder;
    }

    @Override
    public EmbedBuilder simpleAuthoredEmbed(User user, String title, String description, Color color) {
        return new EmbedBuilder()
                .setAuthor(user.getEffectiveName(), user.getAvatarUrl(), user.getAvatarUrl())
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#2F3136"))
                .setTimestamp(Instant.now())
                .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
    }

    @Override
    public EmbedBuilder simpleAuthoredEmbed() {
        return new EmbedBuilder()
                .setColor(Color.decode("#2F3136"))
                .setTimestamp(Instant.now())
                .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
    }

    @Override
    public EmbedBuilder simpleEmbed(String title, String description, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#2F3136"))
                .setTimestamp(Instant.now())
                .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
    }

    @Override
    public EmbedBuilder info(String icon, String title, String description) {
        if (icon.isEmpty()) {
            return this.simpleEmbed(title, description, Color.decode("#2F3136"))
                    .setTitle(title)
                    .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
        }
        return this.simpleEmbed(title, description, Color.decode("#2F3136"))
                .setTitle(String.format("%s %s", this.instance.getEmojiConfig().get(icon), title))
                .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl());
    }

    @Override
    public EmbedBuilder warn(String icon, String title, String description) {
        return this.info(icon, title, description)
                .setColor(Color.YELLOW);
    }

    @Override
    public EmbedBuilder error(String icon, String title, String description) {
        return this.info(icon, title, description)
                .setColor(Color.RED);
    }

    @Override
    public EmbedBuilder image(String title, String description, String url) {
        return this.info("", title, description)
                .setImage(url)
                .setColor(Color.decode("#2F3136"));
    }
}
