/*
---------------------------------------------------------------------------------
File Name : VerificationBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.verification;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VerificationBuilder {
    private String userId;
    private String status;
    private String verifiedBy;
    private String verificationCode;
    private JsonObject answers;
}