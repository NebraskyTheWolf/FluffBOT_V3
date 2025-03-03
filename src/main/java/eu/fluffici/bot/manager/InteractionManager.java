package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : InteractionManager.java

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
import eu.fluffici.bot.api.interactions.ActionRowBuilder;
import eu.fluffici.bot.api.hooks.IInteractionManager;
import eu.fluffici.bot.api.interactions.InteractionBuilder;
import eu.fluffici.bot.components.button.KeypadButton;
import eu.fluffici.bot.api.interactions.Interactions;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractionManager implements IInteractionManager {
    private final FluffBOT fluffbot;

    public InteractionManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    @Override
    public Button newInteraction(InteractionBuilder builder) {
        try {
            this.fluffbot.getGameServiceManager().newInteraction(builder.toInteraction());

            return Button.of(builder.getStyle(), String.format("%s#%s", builder.getButtonId(), builder.getInteractionId()) , builder.getButtonName());
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to create interaction", e);
            e.printStackTrace();
        }
        return Button.danger("error", "Unable to create interaction.");
    }

    public ActionRowBuilder newInteractions(InteractionBuilder builder, List<Button> buttons) {
        try {
            this.fluffbot.getGameServiceManager().newInteraction(builder.toInteraction());

            AtomicInteger i = new AtomicInteger();
            buttons.forEach(btn -> btn.withId(String.format("%s_%s#%s", builder.getButtonId(), i.getAndIncrement(), builder.getInteractionId())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Button> sectionOne = new ArrayList<>();
        List<Button> sectionTwo = new ArrayList<>();
        List<Button> sectionThree = new ArrayList<>();

        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            if (i < 3) {
                sectionOne.add(btn);
            } else if (i < 6) {
                sectionTwo.add(btn);
            } else {
                sectionThree.add(btn);
            }
        }

        return ActionRowBuilder.builder()
                .sectionOne(sectionOne)
                .sectionTwo(sectionTwo)
                .sectionThree(sectionThree)
                .build();
    }

    @Override
    public ActionRowBuilder buildKeypad(UserSnowflake user) {
        List<Button> buttons = new ArrayList<>();

        for (int i = 1; i <= 9; i++) {
            ButtonStyle style = (i % 2 == 0) ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY;
            buttons.add(new KeypadButton(String.format("row_key_%s", i), String.format("%s", i), style).build(null));
        }

        return this.newInteractions(InteractionBuilder
                .builder()
                .buttonId("row_keypad")
                .buttonName("")
                .style(ButtonStyle.DANGER)
                .userId(user.getId())
                .customId("row_keypad")
                .isDm(false)
                .isExpired(false)
                .isAcknowledged(false)
                .isAttached(false)
                .isUpdated(false)
                .interactionId(UUID.randomUUID().toString())
                .expiration(new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()))
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build(),
                buttons
        );
    }

    @Override
    public Interactions fetchInteraction(String interaction) {
        try {
            return this.fluffbot.getGameServiceManager().fetchInteraction(interaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateInteraction(Interactions payload) {
        try {
            this.fluffbot.getGameServiceManager().updateInteraction(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAcknowledged(Interactions interactions) {
        try {
            this.fluffbot.getGameServiceManager().setAcknowledged(interactions.getUserId());
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Cannot acknowledge '" + interactions.getInteractionId() + "' interaction.", e);
            e.printStackTrace();
        }
    }

    @Override
    public void setAttached(Interactions interactions) {
        try {
            this.fluffbot.getGameServiceManager().setAttached(interactions);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Cannot attach '" + interactions.getInteractionId() + "' message.", e);
            e.printStackTrace();
        }
    }
}
