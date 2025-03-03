/*
---------------------------------------------------------------------------------
File Name : SanctionBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 09/07/2024
Last Modified : 09/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import eu.fluffici.bot.api.furraid.SanctionType;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import java.sql.Timestamp;

@Getter
@Builder
public class SanctionBuilder {
    private Guild guild;
    private Member author;
    private Member member;
    private String reason;
    private SanctionType sanctionType;
    private Timestamp expiration;
}