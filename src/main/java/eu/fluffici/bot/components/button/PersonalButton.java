package eu.fluffici.bot.components.button;

/*
---------------------------------------------------------------------------------
File Name : PersonalButton.java

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
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.api.interactions.Interactions;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.tuple.Pair;

@Getter
public abstract class PersonalButton extends ButtonBuilder {
    private final FluffBOT instance = FluffBOT.getInstance();
    public PersonalButton(String customId, String label, ButtonStyle style) {
        super(customId, label, style);
    }

    @Override
    @SneakyThrows
    public void execute(ButtonInteraction interaction) {
        Pair<Boolean, Pair<String, Interactions>> result = this.handleInteraction(interaction.getUser().getId());
        if (result.getLeft()) {
            Interactions interactions = result.getRight().getRight();
            this.handle(interaction);

            this.getInteractionManager().setAcknowledged(interactions);
            interactions.setMessageId(interaction.getMessageId());
            this.getInteractionManager().setAttached(interactions);
        } else {
            interaction.reply(result.getRight().getLeft()).queue();
        }
    }

    public abstract void handle(ButtonInteraction interaction);
}
