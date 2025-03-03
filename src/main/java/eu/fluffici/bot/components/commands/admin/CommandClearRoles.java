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
import eu.fluffici.bot.api.beans.roles.PurchasableRoles;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
public class CommandClearRoles extends Command {
    public CommandClearRoles() {
        super("clear-purchasable", "Remove the purchasable roles from all members.", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        handleConfirmation(interaction, "Are you sure to remove all the members from the purchasable roles?", new ConfirmCallback() {
            @Override
            public void confirm(ButtonInteraction interaction) throws Exception {
                interaction.deferReply(true).queue();

                List<PurchasableRoles> purchasableRoles = FluffBOT.getInstance()
                        .getGameServiceManager().fetchPurchasableRoles();

                if (purchasableRoles.isEmpty()) {
                    interaction.getHook().sendMessageEmbeds(buildError("No purchasable roles found.")).setEphemeral(true).queue();
                    return;
                }

                List<Role> roles = new ArrayList<>();

                for (PurchasableRoles role : purchasableRoles) {
                    Role role1 = interaction.getJDA().getRoleById(role.getRoleId());
                    if (role1 != null) {
                        roles.add(role1);
                    }
                }

                if (roles.isEmpty()) {
                    interaction.getHook().sendMessageEmbeds(buildError("Purchasable roles was found but the cache pool does not recognise those roles.")).setEphemeral(true).queue();
                    return;
                }

                if (interaction.getGuild() != null) {
                    List<Member> members = interaction.getGuild().getMembersWithRoles(roles);

                    CompletableFuture.runAsync(() -> {
                        for (Member member : members) {
                            for (Role role : roles) {
                                if (member.getRoles().contains(role)) {
                                    interaction.getGuild().removeRoleFromMember(member, role).reason("Clearing out members from the purchasable roles.").queue();
                                }
                            }
                        }
                    }).whenComplete(((unused, throwable) -> interaction.getHook().sendMessageEmbeds(buildSuccess("Roles cleared successfully!")).setEphemeral(true).queue())).get(30, TimeUnit.SECONDS);
                } else {
                    interaction.getHook().sendMessageEmbeds(buildSuccess("Unable to find the guild.")).setEphemeral(true).queue();
                }
            }

            @Override
            public void cancel(ButtonInteraction interaction) throws Exception { interaction.replyEmbeds(buildSuccess("Role clearing cancelled!")).setEphemeral(true).queue(); }
        });
    }
}
