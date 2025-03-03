/*
---------------------------------------------------------------------------------
File Name : VerificationReminder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 17/06/2024
Last Modified : 17/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class VerificationReminder {
    private String userId;
    private boolean isLocked;
    private boolean isNotified;
    private Timestamp createdAt;
    private Timestamp expireAt;
}