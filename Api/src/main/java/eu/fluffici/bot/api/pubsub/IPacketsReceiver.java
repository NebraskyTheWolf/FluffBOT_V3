/*
---------------------------------------------------------------------------------
File Name : IPacketsReceiver

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.pubsub;

public interface IPacketsReceiver
{
    /**
     * Fired when a Redis PubSub message is received
     *
     * @param channel PubSub message's channel
     * @param packet PubSub message's content
     */
    void receive(String channel, String packet);
}