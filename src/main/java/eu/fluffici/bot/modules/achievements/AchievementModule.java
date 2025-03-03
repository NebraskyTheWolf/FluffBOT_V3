package eu.fluffici.bot.modules.achievements;

/*
---------------------------------------------------------------------------------
File Name : AchievementModule.java

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
import eu.fluffici.bot.api.module.Category;
import eu.fluffici.bot.api.module.Module;
import eu.fluffici.bot.modules.achievements.impl.AchievementManager;
import lombok.NonNull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
public class AchievementModule extends Module {

    private AchievementManager achievementManager;

    private final FluffBOT instance;

    public AchievementModule(FluffBOT instance) {
        super("achievement", "Achievement", "Handling the user achievements", "1.0.0", "Vakea", Category.SYSTEM);

        this.instance = instance;
    }

    @Override
    public void onEnable() {
        this.achievementManager = new AchievementManager(FluffBOT.getInstance());
        this.instance.setAchievementManager(this.achievementManager);
    }

    @Override
    public void onDisable() {
        this.logger.warn("Disabling Achievement modules.");
    }

    /**
     * This method is called when a message is received.
     *
     * @param event The event object containing information about the message received event.
     */
    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;

        this.achievementManager.incrementAchievements(event.getAuthor().getId(), new int[]{ 1, 2, 3, 4, 5 }, 1);
    }

    /**
     * This method is called when a reaction is added to a message.
     *
     * @param event The event object containing information about the reaction add event.
     */
    @Override
    public void onMessageReactionAdd(@NonNull MessageReactionAddEvent event) {
        if (event.getUser().isBot() || event.getUser().isSystem())
            return;

        this.achievementManager.incrementAchievements(event.getUserId(), new int[]{ 7, 8, 9, 10, 11, 12 }, 1);
    }
}
