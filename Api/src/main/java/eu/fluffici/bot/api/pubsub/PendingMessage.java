/*
---------------------------------------------------------------------------------
File Name : PendingMessage

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.pubsub;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingMessage
{
    private final String channel;
    private final String message;
    private final Runnable callback;

    /**
     * Constructor
     *
     * @param channel Message's channel
     * @param message Message's content
     * @param callback Callback fired after the operation
     */
    public PendingMessage(String channel, String message, Runnable callback)
    {
        this.channel = channel;
        this.message = message;
        this.callback = callback;
    }

    /**
     * Constructor
     *
     * @param channel Message's channel
     * @param message Message's content
     */
    public PendingMessage(String channel, String message)
    {
        this(channel, message, null);
    }

    /**
     * Fire callback
     */
    public void runAfter()
    {
        try
        {
            if (this.callback != null)
                this.callback.run();
        }
        catch (Exception ignored) {}
    }
}