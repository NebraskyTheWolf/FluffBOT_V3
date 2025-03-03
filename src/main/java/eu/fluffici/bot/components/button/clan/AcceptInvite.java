package eu.fluffici.bot.components.button.clan;

/*
---------------------------------------------------------------------------------
File Name : AcceptInvite.java

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


import eu.fluffici.bot.components.button.PersonalButton;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.clans.ClanRequestBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.beans.players.UserClanBean;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class AcceptInvite extends PersonalButton {
    public AcceptInvite() {
        super("row_accept_invite", "Accept invite", ButtonStyle.SECONDARY);
    }

    @Override
    public void handle(ButtonInteraction interaction) {
        try {
            ClanRequestBean activeRequest = this.getInstance().getGameServiceManager().getActiveRequest(interaction.getUser().getId());
            if (activeRequest == null) {
                interaction.reply(this.getLanguageManager().get("common.clan.no_invite_found")).queue();
                return;
            }

            PlayerBean player = this.getUserManager().fetchUser(interaction.getUser());

            switch (activeRequest.getStatus()) {
                case "ACTIVE" -> {
                    if (this.getUserManager().hasClan(player)) {
                        interaction.reply(this.getLanguageManager().get("common.clan.already_in_clan")).queue();
                    } else {
                        this.getUserManager().updateClan(new UserClanBean(
                                player.getUserId(),
                                activeRequest.getClanId()
                        ));

                        this.getInstance().getClanManager().addClanMember(new ClanMembersBean(
                                activeRequest.getClanId(),
                                player.getUserId(),
                                "Member"
                        ));
                        
                        this.getInstance().getClanManager().acknowledgeInvite(activeRequest.getInviteId());
                        interaction.reply(this.getLanguageManager().get("common.clan.invite_accepted")).queue();
                    }
                }
                case "ACKNOWLEDGED" -> interaction.reply(this.getLanguageManager().get("common.clan.already_acknowledged")).queue();
                case "EXPIRED" -> interaction.reply(this.getLanguageManager().get("common.clan.expired")).queue();
                default -> interaction.reply(this.getLanguageManager().get("common.clan.unknown_status")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
