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

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationParser {
    private long id;
    private String userId;
    private String username;
    private String avatarUrl;
    private String status;
    private List<Question> questions;
    private List<UserInfo> userInfo;
    private Timestamp createdAt;
    private JsonObject verifiedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Question {
        private String title;
        private String answer;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private String title;
        private String data;
    }
}