package eu.fluffici.bot.components.modal.clan;

/*
---------------------------------------------------------------------------------
File Name : ModalClanCreate.java

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
import eu.fluffici.bot.api.interactions.Modal;
import eu.fluffici.bot.database.GameServiceManager;
import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.players.UserClanBean;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;

import java.util.UUID;
public class ModalClanCreate extends Modal {
    public ModalClanCreate() {
        super("row_modal_create_clan");
    }

    @Override
    public void execute(ModalInteraction interaction) {
        String title = interaction.getValue("row_clan_title").getAsString();
        String prefix = interaction.getValue("row_clan_prefix").getAsString();
        String description = interaction.getValue("row_clan_description").getAsString();

        String clanId = UUID.randomUUID().toString();

        try {
            GameServiceManager gameServiceManager = FluffBOT.getInstance().getGameServiceManager();
            if (gameServiceManager.hasPrefix(prefix)
                    || gameServiceManager.hasTitle(title)) {
                interaction.reply(this.getLanguageManager().get("common.clan.already_exists")).setEphemeral(true).queue();
            } else {
                FluffBOT.getInstance().getClanManager().createClan(new ClanBean(
                        interaction.getUser().getId(),
                        clanId,
                        title,
                        prefix,
                        description,
                        "",
                        "#ff5733"
                ));

                FluffBOT.getInstance().getClanManager().addClanMember(new ClanMembersBean(
                        clanId,
                        interaction.getUser().getId(),
                        "OWNER"
                ));

                FluffBOT.getInstance().getGameServiceManager().updateUserClan(new UserClanBean(
                        interaction.getUser().getId(),
                        clanId
                ));

                FluffBOT.getInstance().getUserManager().removeTokens(
                        FluffBOT.getInstance().getUserManager().fetchUser(interaction.getUser()),
                        50
                );

                interaction.reply(this.getLanguageManager().get("common.clan.created")).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            interaction.reply(this.getLanguageManager().get("common.clan.error_while_saving")).setEphemeral(true).queue();
        }
    }
}
