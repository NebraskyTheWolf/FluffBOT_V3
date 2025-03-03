/*
---------------------------------------------------------------------------------
File Name : TelegramVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * The TelegramVerification class represents the verification details for a user on Telegram.
 * It contains the user ID, verification code, and the timestamps indicating when the verification
 * was created and updated.
 *
 * <p>
 * This class provides getters and setters for all the attributes to allow accessing and modifying
 * the verification details.
 * </p>
 *
 * <p>
 * Note: A TelegramVerification object is typically used to hold the verification details of a user
 * on Telegram and is not responsible for performing any verification logic itself.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * TelegramVerification verification = new TelegramVerification(userId, verificationCode, createdAt, updatedAt);
 * Long userId = verification.getUserId();
 * verification.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
 * </pre>
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
public class TelegramVerification {
    private Long userId;
    private String username;
    private String verificationCode;
    private VerificationStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}