package eu.fluffici.bot.components.button;

/*
---------------------------------------------------------------------------------
File Name : ButtonManager.java

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
import eu.fluffici.bot.api.interactions.InteractionBuilder;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.api.interactions.SelectMenu;
import eu.fluffici.bot.components.button.achievements.AchievementSelectMenu;
import eu.fluffici.bot.components.button.beta.JoinBetaButton;
import eu.fluffici.bot.components.button.clan.AcceptInvite;
import eu.fluffici.bot.components.button.misc.ButtonLibrariesUsed;
import eu.fluffici.bot.components.button.owner.ButtonMigration;
import eu.fluffici.bot.components.button.quarantine.SelectQuarantineActions;
import eu.fluffici.bot.components.button.relationship.RelationshipAccept;
import eu.fluffici.bot.components.button.support.ButtonCloseTicket;
import eu.fluffici.bot.components.button.support.ButtonOpenTicket;
import eu.fluffici.bot.components.button.verification.ButtonVerification;
import eu.fluffici.bot.components.button.verification.ReminderKick;
import eu.fluffici.bot.components.button.verification.ReminderRemind;
import eu.fluffici.bot.components.button.verification.SelectVerificationActions;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ButtonManager {
    private final List<ButtonBuilder> buttonBuilders = new CopyOnWriteArrayList<>();
    @Getter
    private final List<SelectMenu<?>> selectMenus = new CopyOnWriteArrayList<>();

    private final Object[] lock = new Object[] {};

    public void load() {
        synchronized (this.lock) {
            this.buttonBuilders.clear();
            this.selectMenus.clear();

            this.buttonBuilders.add(new AcceptInvite());
            this.buttonBuilders.add(new RelationshipAccept());

            this.buttonBuilders.add(new ButtonMigration());
            this.buttonBuilders.add(new ButtonLibrariesUsed());

            this.buttonBuilders.add(new ButtonVerification());

            this.buttonBuilders.add(new ButtonCloseTicket());
            this.buttonBuilders.add(new ButtonOpenTicket());

            this.buttonBuilders.add(new ReminderKick());
            this.buttonBuilders.add(new ReminderRemind());

            this.selectMenus.add(new SelectVerificationActions());
            this.selectMenus.add(new SelectQuarantineActions());

            // Temporary button.
            this.buttonBuilders.add(new JoinBetaButton());

            this.selectMenus.add(new AchievementSelectMenu());

            this.buttonBuilders.forEach(cmd -> {
                cmd.setEmbed(FluffBOT.getInstance().getEmbed());
                cmd.setLanguageManager(FluffBOT.getInstance().getLanguageManager());
                cmd.setUserManager(FluffBOT.getInstance().getUserManager());
                cmd.setInteractionManager(FluffBOT.getInstance().getInteractionManager());
            });

            this.selectMenus.forEach(cmd -> {
                cmd.setEmbed(FluffBOT.getInstance().getEmbed());
                cmd.setLanguageManager(FluffBOT.getInstance().getLanguageManager());
                cmd.setUserManager(FluffBOT.getInstance().getUserManager());
                cmd.setInteractionManager(FluffBOT.getInstance().getInteractionManager());

                FluffBOT.getInstance().getJda().addEventListener(cmd);
            });
        }
    }

    public List<ButtonBuilder> getAllButtons() {
        return this.buttonBuilders;
    }

    public ButtonBuilder findByName(String customId) {
        AtomicReference<ButtonBuilder> commandReference = new AtomicReference<>();

        this.buttonBuilders.forEach(cmd -> {
            String id = customId;
            if (id.indexOf('#') != -1) {
                id = id.split("#")[0];
            }

            if (cmd.getCustomId().equals(id)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }

    public SelectMenu<?> findSelectByName(String customId) {
        AtomicReference<SelectMenu<?>> commandReference = new AtomicReference<>();

        this.selectMenus.forEach(cmd -> {
            String id = customId;
            if (id.indexOf('#') != -1) {
                id = id.split("#")[0];
            }

            if (cmd.getCustomId().equals(id)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }

    public Pair<Button, String> toInteraction(String customId, UserSnowflake userSnowflake, TextChannel channel, boolean isDm) {
        ButtonBuilder builder = this.findByName(customId);

        if (builder == null) {
            return Pair.of(Button.of(ButtonStyle.DANGER, customId, "Unable to find " + customId), null);
        }

        String interactionId = UUID.randomUUID().toString();
        Button button =  FluffBOT.getInstance().getInteractionManager()
                .newInteraction(InteractionBuilder
                        .builder()
                        .buttonId(builder.getCustomId())
                        .buttonName(builder.getLabel())
                        .userId(userSnowflake.getId())
                        .customId(builder.getCustomId())
                        .style(builder.getStyle())
                        .isDm(isDm)
                        .channelId(channel != null ? channel.getId() : "")
                        .isExpired(false)
                        .isAcknowledged(false)
                        .isAttached(false)
                        .isUpdated(false)
                        .interactionId(interactionId)
                        .expiration(new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()))
                        .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                        .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                        .build()
                );

        return Pair.of(button, interactionId);
    }
}
