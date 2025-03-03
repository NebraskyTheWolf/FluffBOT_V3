/*
---------------------------------------------------------------------------------
File Name : DatabaseConnector

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.redis;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.pubsub.RedisServer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

public class DatabaseConnector
{

    private final FluffBOT instance;
    private JedisPool cachePool;
    private RedisServer bungee;

    public DatabaseConnector(FluffBOT instance)
    {
        this.instance = instance;
    }

    public DatabaseConnector(FluffBOT instance, RedisServer bungee)
    {
        this.instance = instance;
        this.bungee = bungee;

        initiateConnection();
    }

    public Jedis getResource()
    {
        return cachePool.getResource();
    }

    public void killConnection()
    {
        cachePool.close();
        cachePool.destroy();
    }

    private void initiateConnection() {
        connect();

        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                cachePool.getResource().close();
            } catch (Exception e) {
                e.printStackTrace();
                this.instance.getLogger().error("Error redis connection, Try to reconnect!", e);
                connect();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void connect()
    {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(-1);
        config.setJmxEnabled(false);

        try
        {
            this.cachePool = new JedisPool(config, this.bungee.getIp(), this.bungee.getPort(), 0, this.bungee.getPassword());
            this.cachePool.getResource().close();

            this.instance.getLogger().info("Connected to database.");
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Can't connect to the database!", e);
        }
    }

}