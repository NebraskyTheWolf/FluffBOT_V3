/*
---------------------------------------------------------------------------------
File Name : AuditLogGenerator

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 28/06/2024
Last Modified : 28/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.furraid;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuditLogGenerator {
    private final List<AuditLogEntry> auditLogEntries = new ArrayList<>();

    public void addEntry(AuditLogEntry entry) {
        auditLogEntries.add(entry);
    }
}