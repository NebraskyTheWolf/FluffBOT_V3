/*
---------------------------------------------------------------------------------
File Name : RedisServer

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
public class RedisServer {
    private final String ip;
    private final int port;
    private final String password;

    public RedisServer(String ip, int port, String password)
    {
        this.ip = ip;
        this.port = port;
        this.password = password;
    }
}