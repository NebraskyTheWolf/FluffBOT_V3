package eu.fluffici.bot.components.commands.profile;

/*
---------------------------------------------------------------------------------
File Name : CommandAchievement.java

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
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.beans.achievements.AchievementCategoryBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

@CommandHandle
@SuppressWarnings("All")
public class CommandAchievement extends Command {

    private final FluffBOT instance;

    public CommandAchievement(FluffBOT instance) {
        super("achievements", "your achievements progresses", CommandCategory.PROFILE);

        this.instance = instance;

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        List<AchievementCategoryBean> achievementCategories = this.instance
                .getGameServiceManager()
                .getAchievementCategories();

        StringSelectMenu.Builder categories = StringSelectMenu.create("row_achievement_categories");
        achievementCategories.forEach(achievementCategory -> categories.addOption(achievementCategory.getCategoryName(), "row_select:category:" + achievementCategory.getCategoryId(), achievementCategory.getCategoryDescription(),
                Emoji.fromUnicode(achievementCategory.getIcon()))
        );

        interaction.replyEmbeds(this.getEmbed().simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("common.achievements"), "https://fluffici.eu",ICON_QUESTION_MARK)
                        .setDescription(this.getLanguageManager().get("common.select"))
                        .build())
                .addActionRow(categories.build())
                .setEphemeral(true)
                .queue();
    }
}
