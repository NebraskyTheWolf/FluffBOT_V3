package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandClearRoles.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

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


import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
public class CommandPurge extends Command {
    public CommandPurge() {
        super("purge", "Purge the chat message(s)", CommandCategory.ADMINISTRATOR);

        this.getOptionData().add(new OptionData(OptionType.INTEGER, "amount", "The amount of messages to delete", true)
                .setMaxValue(100)
                .setMinValue(1)
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        int amount = interaction.getOption("amount").getAsInt();

        handleConfirmation(interaction,
                this.getLanguageManager().get("command.purge.confirm", amount),
                this.getLanguageManager().get("command.purge.confirm.button", amount),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        interaction.deferEdit().queue();
                        interaction.getChannel().getIterableHistory().takeAsync(amount).thenAccept(messages -> {
                            int i = 0;
                            for (Message message : messages) {
                                message.delete().queueAfter(1, TimeUnit.SECONDS);
                                i++;

                                Button button = Button.secondary("button:display", String.format("%s/%s purged.", i, amount)).asDisabled();
                                if (i >= amount) {
                                    button.withLabel("Purge completed.").withStyle(ButtonStyle.SUCCESS);
                                }

                                interaction.getHook().editOriginalEmbeds(buildSuccess(getLanguageManager().get("command.purge.in_progress"))).setActionRow(button).queueAfter(2, TimeUnit.SECONDS);
                            }
                        }).exceptionally(throwable -> {
                            throwable.printStackTrace();
                            return null;
                        });
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.purge.cancel"))).setEphemeral(true).queue();
                    }
                }, false, false);
    }
}
