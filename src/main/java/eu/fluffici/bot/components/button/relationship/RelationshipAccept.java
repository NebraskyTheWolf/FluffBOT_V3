package eu.fluffici.bot.components.button.relationship;

/*
---------------------------------------------------------------------------------
File Name : RelationshipAccept.java

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


import eu.fluffici.bot.api.beans.players.RelationshipInviteBuilder;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.button.PersonalButton;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import javax.management.relation.Relation;

public class RelationshipAccept extends PersonalButton {
    public RelationshipAccept() {
        super("row_accept_relationship", "Accept invite", ButtonStyle.PRIMARY);
    }

    @Override
    @SneakyThrows
    public void handle(ButtonInteraction interaction) {
        User user = interaction.getUser();

        if (this.getUserManager().fetchRelationship(user, user)) {
            interaction.reply(this.getLanguageManager().get("common.relationship.button.already_in_relationship")).queue();
        } else {
            RelationshipInviteBuilder relationshipInvite = this.getUserManager().getActiveRel(user);

            if (relationshipInvite != null) {
                RelationshipInviteBuilder relationshipBuilder = this.getUserManager().getActiveRel(user);

                this.getUserManager().updateRelationships(interaction.getHook().getJDA().getUserById(relationshipBuilder.getRelationshipOwner().getId()), user);
                this.getUserManager().acknowledgeInvite(user);

                PlayerBean playerBean = this.getUserManager().fetchUser(user);

                playerBean.setBoundTo(relationshipBuilder.getRelationshipOwner().getId());
                this.getInstance().getGameServiceManager().updatePlayer(playerBean);

                interaction.reply(this.getLanguageManager().get("common.relationship.button.accepted")).queue();
            } else {
                interaction.reply(this.getLanguageManager().get("common.relationship.button.not_found")).queue();
            }
        }
    }
}
