package eu.fluffici.bot.components.commands.developer;

/*
---------------------------------------------------------------------------------
File Name : CommandSpawn.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.interactions.*;
import eu.fluffici.bot.components.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@CommandHandle
public class CommandSpawn extends Command {
    public CommandSpawn() {
        super("spawn", "Spawn button", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);

        this.getOptionData().add(new OptionData(OptionType.STRING, "customid", "The button custom id.", true));
        this.getOptionData().add(new OptionData(OptionType.BOOLEAN, "interaction", "Is this button a personal interaction?", true));
        this.getOptionData().add(new OptionData(OptionType.BOOLEAN, "keypad", "Generate a test keypad?", false));

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String customId = interaction.getOption("customid").getAsString();
        boolean personal = interaction.getOption("interaction").getAsBoolean();
        boolean keypad = interaction.getOption("keypad").getAsBoolean();

        ButtonBuilder builder = FluffBOT.getInstance().getButtonManager().findByName(customId);
        if (builder != null) {
            Button button = null;
            if (personal && !keypad) {
                button = FluffBOT.getInstance()
                        .getInteractionManager()
                        .newInteraction(InteractionBuilder
                                .builder()
                                .buttonId(builder.getCustomId())
                                .buttonName(builder.getLabel())
                                .userId(interaction.getUser().getId())
                                .customId(builder.getCustomId())
                                .style(builder.getStyle())
                                .isDm(false)
                                .isExpired(false)
                                .isAcknowledged(false)
                                .isAttached(false)
                                .isUpdated(false)
                                .interactionId(UUID.randomUUID().toString())
                                .expiration(new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()))
                                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                                .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                                .build()
                        );
            } else if (keypad) {
                ActionRowBuilder builder1 = FluffBOT.getInstance()
                        .getInteractionManager()
                        .buildKeypad(interaction.getUser());

                interaction.reply("OWO")
                        .addActionRow(builder1.getSectionOne())
                        .addActionRow(builder1.getSectionTwo())
                        .addActionRow(builder1.getSectionThree()).queue();
            } else {
                button = builder.build(null);
            }
            interaction.reply("Test button.").setActionRow(button).queue();
        } else {
            interaction.reply(String.format("Button '%s' does not exists.", customId)).queue();
        }
    }
}
