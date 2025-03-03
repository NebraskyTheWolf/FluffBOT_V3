/*
---------------------------------------------------------------------------------
File Name : SecurityListener

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database;

import eu.fluffici.bot.api.pubsub.IPacketsReceiver;

/**
 * The SecurityListener class is an implementation of the IPacketsReceiver interface.
 * It provides a method to receive Redis PubSub messages.
 */
public class SecurityListener implements IPacketsReceiver {
    @Override
    public void receive(String channel, String packet) {}
}