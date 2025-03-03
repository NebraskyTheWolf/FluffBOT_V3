/*
---------------------------------------------------------------------------------
File Name : AuditLogEntry

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 28/06/2024
Last Modified : 28/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.furraid;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.Timestamp;

@Getter
public class AuditLogEntry {
    private final Guild guild;
    private final Member author;
    private final String action;
    private final Object oldValue;
    private final Object newValue;
    private final Timestamp updatedAt;

    public AuditLogEntry(Guild guild, Member author, String action, Object oldValue, Object newValue, Timestamp updatedAt) {
        this.guild = guild;
        this.author = author;
        this.action = String.format("The value of '%s' was changed", action);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.updatedAt = updatedAt;
    }
}