/*
---------------------------------------------------------------------------------
File Name : ISender

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.pubsub;

public interface ISender
{
    /**
     * Publishes a pending message.
     *
     * @param message The pending message to be published.
     */
    void publish(PendingMessage message);
}