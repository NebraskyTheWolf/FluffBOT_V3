/*
---------------------------------------------------------------------------------
File Name : IPatternReceiver

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.pubsub;

public interface IPatternReceiver
{
    /**
     * Fired when a Redis PubSub message is received
     *
     * @param pattern PubSub message's pattern
     * @param channel PubSub message's channel
     * @param packet PubSub message's content
     */
    void receive(String pattern, String channel, String packet);
}