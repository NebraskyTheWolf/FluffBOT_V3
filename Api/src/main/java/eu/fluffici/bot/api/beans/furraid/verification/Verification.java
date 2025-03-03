/*
---------------------------------------------------------------------------------
File Name : Verification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 27/06/2024
Last Modified : 27/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid.verification;

import com.google.gson.JsonObject;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Verification {
    private long id;
    private String guildId;
    private String userId;
    private String status;
    private String verifiedBy;
    private String verificationCode;
    private String messageId;
    private List<VerificationParser.Question> answers;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}