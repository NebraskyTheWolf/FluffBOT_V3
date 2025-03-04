/*
---------------------------------------------------------------------------------
File Name : NotifyReminder

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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_WARNING;

public class NotifyReminder extends Task {
    private final Map<UserSnowflake, Long> lastNotifiedTime = new ConcurrentHashMap<>();

    /**
     * Executes the notify reminder task.
     * This method schedules a task to run at a fixed rate. The task fetches all reminders from the game service manager and
     * iterates over each user and their reminder hours. If the reminder hours are less than or equal to 12 and greater than 0,
     * it triggers a notification for the user. It also checks if the time elapsed since the last notification for the user is
     * greater than or equal to 12 hours. If so, it handles the reminder for the user and updates the last notified time.
     * This method is overridden from the parent class Task.
     */
    @Override
    public void execute() {
        FluffBOT.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            Map<UserSnowflake, Long> reminders = FluffBOT.getInstance().getGameServiceManager().fetchAllReminders();

            reminders.forEach((user, hours) -> {
                long currentTime = System.currentTimeMillis();
                long lastNotifyTime = lastNotifiedTime.getOrDefault(user, 0L);

                if (hours <= 12 && hours > 0 && (currentTime - lastNotifyTime >= TimeUnit.HOURS.toMillis(24))) {
                    this.handleReminder(user, hours);
                    lastNotifiedTime.put(user, currentTime);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Handles a reminder for a user.
     *
     * @param user The UserSnowflake object representing the user for whom the reminder is being handled.
     * @param remainingTime The remaining time in hours for the reminder.
     */
    @SuppressWarnings("All")
    private void handleReminder(@NotNull UserSnowflake user, long remainingTime) {
        LanguageManager languageManager = FluffBOT.getInstance().getLanguageManager();

        PrivateChannel channel = FluffBOT.getInstance()
                .getJda()
                .getUserById(user.getId())
                .openPrivateChannel()
                .complete();

        if (channel.canTalk()) {
            channel.sendMessageEmbeds(FluffBOT.getInstance().getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(languageManager.get("task.reminder.notify.title"), "https://fluffici.eu", ICON_WARNING.getUrl())
                    .setDescription(languageManager.get("task.reminder.notify.description", remainingTime))
                    .setColor(Color.ORANGE)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue();
        }
    }
}
