package eu.fluffici.bot.components.button.achievements;

/*
---------------------------------------------------------------------------------
File Name : AchievementSelectMenu.java

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
import eu.fluffici.bot.api.achievement.Achievement;
import eu.fluffici.bot.api.beans.achievements.AchievementBean;
import eu.fluffici.bot.api.beans.achievements.AchievementProgressBean;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.SelectMenu;
import eu.fluffici.bot.components.button.paginate.PageBuilder;
import eu.fluffici.bot.components.button.paginate.PaginationBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_MEDAL;
import static eu.fluffici.bot.components.button.paginate.PaginationHandler.handlePagination;

@SuppressWarnings("All")
public class AchievementSelectMenu extends SelectMenu<StringSelectInteraction>  {

    public AchievementSelectMenu() {
        super("row_achievement_categories");
    }

    @Override
    @SneakyThrows
    public void execute(StringSelectInteraction interaction) {
        String argument = interaction.getValues().get(0).split(":")[2];
        var achievementsMap = FluffBOT.getInstance().getAchievementManager().getAchievements()
                .stream()
                .collect(Collectors.toMap(Achievement::getID, Function.identity()));

        List<AchievementProgressBean> progressBeans = FluffBOT.getInstance()
                .getGameServiceManager()
                .getAchievementProgresses(interaction.getUser().getId())
                .stream()
                .filter(progressBean -> {
                    Achievement achievement = achievementsMap.get(progressBean.getAchievementId());
                    return achievement != null && achievement.getParentCategory().getId() == Integer.parseInt(argument);
                })
                .toList();

        if (progressBeans.size() <= 0) {
            interaction.reply(this.getLanguageManager().get("common.achievement.no_progress")).setEphemeral(true).queue();
            return;
        }

        List<Achievement> achievements = progressBeans.stream()
                .map(progressBean -> achievementsMap.get(progressBean.getAchievementId()))
                .filter(Objects::nonNull)
                .toList();

        PaginationBuilder achievementPages = PaginationBuilder
                .builder()
                .pages(new ArrayList<>())
                .paginationUniqueId(GameId.generateId())
                .paginationOwner(interaction.getUser())
                .isEphemeral(true)
                .build();

        prepare(achievements, interaction.getUser(), achievementPages);

        handlePagination(interaction, achievementPages, false);
    }

    private void prepare(@NotNull List<Achievement> currentPageAchievements, User user, PaginationBuilder achievementPages) {
        currentPageAchievements.forEach(achievement -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor(this.getLanguageManager().get("common.achievement.title", user.getEffectiveName()), "https://fluffici.eu", ICON_MEDAL.getUrl());
            embedBuilder.setColor(Color.decode("#2F3136"));

            embedBuilder.setDescription(this.getLanguageManager().get("common.statistics.achievements", achievement.getParentCategory().getDisplayName()));

            try {
                AchievementBean ach = FluffBOT.getInstance().getGameServiceManager().getAchievement(achievement.getID());
                if (!ach.getAchievementDescription().isEmpty() || !ach.getAchievementDescription().isBlank()) {
                    embedBuilder.addField(achievement.getDisplayName(), ach.getAchievementDescription(), true);
                } else {
                    embedBuilder.addField(achievement.getDisplayName(), this.getLanguageManager().get("common.achievement.no_desc"), true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            embedBuilder.addField("Progress", String.valueOf(achievement.getProgress(user.getId()).getProgress()), true);
            embedBuilder.addField("Status", achievement.isUnlocked(user.getId()) ? "\uD83C\uDF1F " +  this.getLanguageManager().get("command.achievement.unlocked") : "\uD83D\uDD12 " + this.getLanguageManager().get("command.achievement.locked"), true);

            embedBuilder.setTimestamp(Instant.now());
            embedBuilder.setFooter("FluffBOT v3", FluffBOT.getInstance().getJda().getSelfUser().getAvatarUrl());

            achievementPages.addPage(PageBuilder
                    .builder()
                    .message(embedBuilder.build())
                    .build()
            );
        });
    }
}
