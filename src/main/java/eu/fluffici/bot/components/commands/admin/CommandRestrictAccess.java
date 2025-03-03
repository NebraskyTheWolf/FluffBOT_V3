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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.RestrictedAccess;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.events.UserRestrictionEvent;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.sql.Timestamp;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
public class CommandRestrictAccess extends Command {
    public CommandRestrictAccess() {
        super("restrict-access", "Restrict the usage of FluffBOT permanently to a specific user.", CommandCategory.ADMINISTRATOR);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select the user to restrict", true));
        this.getOptionData().add(new OptionData(OptionType.STRING, "reason", "Please enter a reason", true));

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        String reason = interaction.getOption("reason").getAsString();

        RestrictedAccess restrictedAccess = FluffBOT.getInstance()
                .getGameServiceManager().fetchRestrictedPlayer(user);

        if (restrictedAccess != null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.restrict.already_restricted"))).setEphemeral(true).queue();
            return;
        }

        handleConfirmation(interaction,
                this.getLanguageManager().get("command.restrict.confirm", user.getAsMention()),
                this.getLanguageManager().get("command.restrict.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        FluffBOT.getInstance().getGameServiceManager().createPlayerRestriction(new RestrictedAccess(
                                user,
                                interaction.getUser(),
                                reason,
                                true,
                                new Timestamp(System.currentTimeMillis())
                        ));

                        FluffBOT.getInstance().getEventBus().post(new UserRestrictionEvent(
                                user,
                                interaction.getUser(),
                                reason
                        ));

                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.restrict.success", user.getAsMention(), reason))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.restrict.cancel"))).setEphemeral(true).queue();
                    }
                }
        );
    }
}
