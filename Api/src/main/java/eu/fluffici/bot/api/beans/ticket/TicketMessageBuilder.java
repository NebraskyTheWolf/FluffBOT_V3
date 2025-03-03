/*
---------------------------------------------------------------------------------
File Name : TicketMessageBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class TicketMessageBuilder {
    private String ticketId;
    private String userId;
    private String messageContent;
    private String messageId;
    private Timestamp createdAt;
}