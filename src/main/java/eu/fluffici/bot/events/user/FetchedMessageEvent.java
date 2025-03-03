/*
---------------------------------------------------------------------------------
File Name : FetchedMessageEvent

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.events.user;

import com.google.common.eventbus.Subscribe;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.channel.AutoReactionBuilder;
import eu.fluffici.bot.api.events.MessageEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchedMessageEvent {

    /**
     * Method to handle the MessageEvent event.
     *
     * @param event The MessageEvent to be handled.
     */
    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        if (event.getMessage().getChannel() instanceof TextChannel channel
                && this.containsHttpsLink(event.getMessage().getContentRaw())
        ) {
            Guild guild = channel.getGuild();
            AutoReactionBuilder autoReactions = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .fetchChannelReaction(channel.getId());

            if (autoReactions != null) {
                for (String reaction : autoReactions.getReactions()) {
                    Emoji emoji = guild.getEmojiById(reaction);

                    if (emoji != null) {
                        event.getMessage().addReaction(emoji).queueAfter(1, TimeUnit.SECONDS);
                    } else {
                        FluffBOT.getInstance().getLogger().warn("%s emoji does not exist is guild %s", reaction, guild.getId());
                    }
                }
            }
        }
    }

    /**
     * Checks if the given content contains a valid HTTPS link.
     *
     * @param content The content to be checked.
     * @return true if the content contains a valid HTTPS link, false otherwise.
     */
    public boolean containsHttpsLink(String content) {
        Pattern pattern = Pattern.compile("(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }
}