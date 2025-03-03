/*
---------------------------------------------------------------------------------
File Name : Subscriber

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.redis;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.pubsub.IPacketsReceiver;
import eu.fluffici.bot.api.pubsub.IPatternReceiver;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Subscriber class extends JedisPubSub and provides methods to register receivers for specific channels and patterns,
 * and handles the message receiving events.
 */
class Subscriber extends JedisPubSub {

    private final HashMap<String, HashSet<IPacketsReceiver>> packetsReceivers = new HashMap<>();
    private final HashMap<String, HashSet<IPatternReceiver>> patternsReceivers = new HashMap<>();

    /**
     * Registers a receiver for a specific channel.
     *
     * @param channel  The channel to register the receiver for.
     * @param receiver The IPacketsReceiver instance to register.
     */
    public void registerReceiver(String channel, IPacketsReceiver receiver) {
        HashSet<IPacketsReceiver> receivers = packetsReceivers.get(channel);
        if (receivers == null)
            receivers = new HashSet<>();
        receivers.add(receiver);
        packetsReceivers.put(channel, receivers);
    }

    /**
     * Registers a pattern receiver for a specific pattern.
     *
     * @param pattern  The pattern to register
     * @param receiver The IPatternReceiver instance to register
     */
    public void registerPattern(String pattern, IPatternReceiver receiver) {
        HashSet<IPatternReceiver> receivers = patternsReceivers.get(pattern);
        if (receivers == null)
            receivers = new HashSet<>();
        receivers.add(receiver);
        patternsReceivers.put(pattern, receivers);
    }

    /**
     * This method is called when a message is received on a specific channel.
     * It retrieves the receivers associated with the channel and calls the {@code receive} method on each receiver, passing the channel and message as parameters.
     * If no receivers are found for the channel, a warning message is logged.
     *
     * @param channel The channel on which the message was received
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        try {
            HashSet<IPacketsReceiver> receivers = packetsReceivers.get(channel);
            if (receivers != null)
                receivers.forEach((IPacketsReceiver receiver) -> receiver.receive(channel, message));
            else
                FluffBOT.getInstance().getLogger().warn("{PubSub} Received message on a channel, but no packetsReceivers were found. (channel: " + channel + ", message:" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is called when a message matching a specified pattern is received.
     * It is responsible for invoking the receive method on the pattern receivers associated with the pattern,
     * passing the pattern, channel, and message as arguments.
     * If no pattern receivers are found for the pattern, a warning message is logged.
     *
     * @param pattern The pattern of the received message
     * @param channel The channel on which the message was received
     * @param message The content of the received message
     */
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        try {
            HashSet<IPatternReceiver> receivers = patternsReceivers.get(pattern);
            if (receivers != null)
                receivers.forEach((IPatternReceiver receiver) -> receiver.receive(pattern, channel, message));
            else
                FluffBOT.getInstance().getLogger().warn("{PubSub} Received pmessage on a channel, but no packetsReceivers were found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the channels to which the subscriber is subscribed.
     *
     * @return An array of strings representing the channels subscribed.
     */
    public String[] getChannelsSubscribed() {
        Set<String> strings = packetsReceivers.keySet();
        return strings.toArray(new String[0]);
    }

    /**
     * Retrieves the patterns to which the subscriber is subscribed.
     *
     * @return An array of strings representing the subscribed patterns.
     */
    public String[] getPatternsSubscribed() {
        Set<String> strings = patternsReceivers.keySet();
        return strings.toArray(new String[0]);
    }
}