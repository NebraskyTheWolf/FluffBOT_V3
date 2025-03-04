/*
---------------------------------------------------------------------------------
File Name : LockExpiredReminder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.scheduler.verification;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.language.LanguageManager;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

@SuppressWarnings("All")
public class LockExpiredReminder extends Task {
    @Override
    public void execute() {
        LanguageManager languageManager = FluffBOT.getInstance().getLanguageManager();

        FluffBOT.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            List<String> mentions = new ArrayList<>();

            FluffBOT.getInstance().getGameServiceManager().fetchExpiredReminders().forEach((user, timestamp) -> {
                mentions.add(user.getAsMention());
                FluffBOT.getInstance()
                        .getGameServiceManager()
                        .lockReminder(user);
            });

            if (!mentions.isEmpty()) {
                FluffBOT.getInstance().getJda().getGuildById(FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild"))
                        .getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.staff"))
                        .sendMessageEmbeds(FluffBOT.getInstance().getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(languageManager.get("task.reminder.locked.title"), "https://fluffici.eu", ICON_QUESTION_MARK.getUrl())
                                .setDescription(String.join("* \n", mentions))
                                .setTimestamp(Instant.now())
                                .build()
                        ).addActionRow(
                                FluffBOT.getInstance().getButtonManager().findByName("row:reminder-kick").build(Emoji.fromCustom("userx", 1252638267871068282L, false)),
                                FluffBOT.getInstance().getButtonManager().findByName("row:reminder-remind").build(Emoji.fromCustom("history", 1252638268978368532L, false))
                        ).queue();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
}