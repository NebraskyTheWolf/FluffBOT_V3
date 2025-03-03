/*
---------------------------------------------------------------------------------
File Name : Sender

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.redis;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.pubsub.ISender;
import eu.fluffici.bot.api.pubsub.PendingMessage;
import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Sender class is responsible for adding pending messages to a queue and sending them using Redis.
 */
class Sender implements Runnable, ISender
{

    private final LinkedBlockingQueue<PendingMessage> pendingMessages = new LinkedBlockingQueue<>();
    private final FluffBOT instance;
    private Jedis jedis;

    public Sender(FluffBOT instance)
    {
        this.instance = instance;
    }

    /**
     * Adds a pending message to the queue of messages to be published.
     *
     * @param message The pending message to be published.
     */
    public void publish(PendingMessage message) {
        pendingMessages.add(message);
    }

    /**
     * Executes the sending of pending messages in a loop. The method continuously takes pending messages
     * from the queue and publishes them using Redis. If an exception occurs while publishing, it calls
     * the fixDatabase() method to fix the database connection and retries.
     */
    @Override
    public void run() {
        fixDatabase();
        while (true) {
            PendingMessage message;
            try {
                message = pendingMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                jedis.close();
                return;
            }

            boolean published = false;
            while (!published) {
                try {
                    jedis.publish(message.getChannel(), message.getMessage());
                    message.runAfter();
                    published = true;
                } catch (Exception e) {
                    fixDatabase();
                }
            }
        }
    }

    /**
     * This method is used to fix the database connection by reconnecting to the redis server.
     * If there is an exception while connecting, it will log an error message and retry after 5 seconds.
     *
     * @throws InterruptedException if the method is interrupted while sleeping
     */
    private void fixDatabase() {
        try {
            jedis = instance.getDatabaseConnector().getResource();
        } catch (Exception e) {
            FluffBOT.getInstance().getLogger().error("[Publisher] Cannot connect to redis server : " + e.getMessage() + ". Retrying in 5 seconds.", e);
            try {
                Thread.sleep(5000);
                fixDatabase();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}