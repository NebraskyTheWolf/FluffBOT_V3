package eu.fluffici.bot.components.modal.clan;

/*
---------------------------------------------------------------------------------
File Name : ModalClanUpdate.java

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
import eu.fluffici.bot.api.hooks.PlayerBean;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
public class ModalClanUpdate extends Modal {
    public ModalClanUpdate() {
        super("row_modal_update_clan");
    }

    @Override
    public void execute(ModalInteraction interaction) {
        String title = interaction.getValue("row_clan_title").getAsString();
        String prefix = interaction.getValue("row_clan_prefix").getAsString();
        String description = interaction.getValue("row_clan_description").getAsString();

        PlayerBean player = FluffBOT.getInstance().getUserManager().fetchUser(interaction.getUser());
        ClanBean currentClan = FluffBOT.getInstance().getClanManager().fetchClan(player);

        boolean hasTitleChanged = !title.equals(currentClan.getTitle());
        boolean hasPrefixChanged = !prefix.equals(currentClan.getPrefix());
        boolean hasDescriptionChanged = !description.equals(currentClan.getDescription());

        try {
            GameServiceManager gameServiceManager = FluffBOT.getInstance().getGameServiceManager();
            if (!hasTitleChanged && !hasPrefixChanged && !hasDescriptionChanged) {
                interaction.reply(this.getLanguageManager().get("common.clan.updated")).setEphemeral(true).queue();
            } else if (hasPrefixChanged && !gameServiceManager.hasPrefix(prefix)) {
                FluffBOT.getInstance().getClanManager().updateClan(new ClanBean(
                        interaction.getUser().getId(),
                        currentClan.getClanId(),
                        currentClan.getTitle(),
                        prefix,
                        currentClan.getDescription(),
                        currentClan.getIconURL(),
                        currentClan.getColor()
                ));

                interaction.reply(this.getLanguageManager().get("common.clan.updated")).setEphemeral(true).queue();
            } else if (hasTitleChanged && !gameServiceManager.hasTitle(prefix)) {
                FluffBOT.getInstance().getClanManager().updateClan(new ClanBean(
                        interaction.getUser().getId(),
                        currentClan.getClanId(),
                        title,
                        currentClan.getPrefix(),
                        currentClan.getDescription(),
                        currentClan.getIconURL(),
                        currentClan.getColor()
                ));

                interaction.reply(this.getLanguageManager().get("common.clan.updated")).setEphemeral(true).queue();
            } else if (hasDescriptionChanged) {
                FluffBOT.getInstance().getClanManager().updateClan(new ClanBean(
                        interaction.getUser().getId(),
                        currentClan.getClanId(),
                        currentClan.getTitle(),
                        currentClan.getPrefix(),
                        description,
                        currentClan.getIconURL(),
                        currentClan.getColor()
                ));

                interaction.reply(this.getLanguageManager().get("common.clan.updated")).setEphemeral(true).queue();
            } else {
                interaction.reply(this.getLanguageManager().get("common.clan.already_exists")).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            interaction.reply(this.getLanguageManager().get("common.clan.error_while_saving")).setEphemeral(true).queue();
        }
    }
}
