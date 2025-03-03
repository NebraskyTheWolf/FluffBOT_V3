/*
---------------------------------------------------------------------------------
File Name : MessagingListener

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
 * The MessagingListener class is responsible for receiving messages sent through the IPacketsReceiver interface.
 * It implements the receive method from the IPacketsReceiver interface.
 */
public class MessagingListener implements IPacketsReceiver {
    @Override
    public void receive(String channel, String packet) {

    }
}