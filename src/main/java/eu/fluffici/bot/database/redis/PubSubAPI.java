/*
---------------------------------------------------------------------------------
File Name : PubSubAPI

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.redis;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.pubsub.*;
import redis.clients.jedis.Jedis;

public class PubSubAPI implements IPubSubAPI
{

    private final Subscriber subscriberPattern;
    private final Subscriber subscriberChannel;

    private final Sender sender;
    private final FluffBOT instance;

    boolean working = true;

    private final Thread senderThread;
    private Thread patternThread;
    private Thread channelThread;

    /**
     * The PubSubAPI class provides methods to subscribe, send messages, and disable the PubSub functionality.
     */
    public PubSubAPI(FluffBOT instance) {
        this.instance = instance;
        subscriberPattern = new Subscriber();
        subscriberChannel = new Subscriber();

        sender = new Sender(instance);
        senderThread = new Thread(sender, "SenderThread");
        senderThread.start();

        startThread();
    }

    /**
     * Starts two threads to handle pattern and channel subscriptions.
     * Each thread connects to the database, retrieves the subscribed patterns/channels,
     * and subscribes to them using Jedis. The threads continue to run while the 'working' flag is true.
     * If an exception occurs during subscription, it is printed to the console.
     */
    private void startThread() {
        patternThread = new Thread(() -> {
            while (working) {
                Jedis jedis = this.instance.getDatabaseConnector().getResource();
                try {
                    String[] patternsSubscribed = subscriberPattern.getPatternsSubscribed();
                    if(patternsSubscribed.length > 0)
                        jedis.psubscribe(subscriberPattern, patternsSubscribed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                jedis.close();
            }
        });
        patternThread.start();

        channelThread = new Thread(() -> {
            while (working)
            {
                Jedis jedis = this.instance.getDatabaseConnector().getResource();
                try
                {
                    String[] channelsSubscribed = subscriberChannel.getChannelsSubscribed();
                    if (channelsSubscribed.length > 0)
                        jedis.subscribe(subscriberChannel, channelsSubscribed);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                jedis.close();
            }
        });
        channelThread.start();
    }

    /**
     * Subscribes a given receiver to a specific channel.
     *
     * This method registers the receiver for the specified channel and checks if there are any existing subscriptions.
     * If there are, it unsubscribes from them.
     *
     * @param channel   The channel to subscribe to.
     * @param receiver  The IPacketsReceiver instance to register.
     */
    @Override
    public void subscribe(String channel, IPacketsReceiver receiver) {
        subscriberChannel.registerReceiver(channel, receiver);
        if(subscriberChannel.isSubscribed())
            subscriberChannel.unsubscribe();
    }

    /**
     * Subscribes a given pattern receiver to a specific pattern. The method registers the pattern receiver for the specified pattern and checks if there are any existing subscriptions
     *. If there are, it unsubscribes from them.
     *
     * @param pattern   The pattern to subscribe to.
     * @param receiver  The IPatternReceiver instance to register.
     */
    @Override
    public void subscribe(String pattern, IPatternReceiver receiver) {
        subscriberPattern.registerPattern(pattern, receiver);
        if(subscriberPattern.isSubscribed())
            subscriberPattern.punsubscribe();
    }

    /**
     * Sends a message to the specified channel.
     *
     * @param channel The channel to send the message to.
     * @param message The message to be sent.
     */
    @Override
    public void send(String channel, String message) {
        sender.publish(new PendingMessage(channel, message));
    }

    /**
     * Sends a given pending message. The message will be published to the appropriate channel.
     *
     * @param message The pending message to be sent.
     */
    @Override
    public void send(PendingMessage message) {
        sender.publish(message);
    }

    /**
     * Returns the message publisher instance.
     *
     * @return The instance of ISender.
     */
    @Override
    public ISender getSender() {
        return sender;
    }

    /**
     * Disables the PubSub API by unsubscribing from all channels and patterns, interrupting the sender and pattern threads, and setting the working flag to false.
     * This method should be called before shutting down the application or when the PubSub functionality is no longer needed.
     * <p>
     * Note: This method may cause interruption of threads and potential loss of data.
     * It is recommended to handle exceptions appropriately and ensure smooth shutdown of the application after calling this method.
     * </p>
     */
    public void disable() {
        working = false;
        subscriberChannel.unsubscribe();
        subscriberPattern.punsubscribe();

        try {
            Thread.sleep(500);
        } catch (Exception ignored) {}

        senderThread.interrupt();
        patternThread.interrupt();
        channelThread.interrupt();
    }
}