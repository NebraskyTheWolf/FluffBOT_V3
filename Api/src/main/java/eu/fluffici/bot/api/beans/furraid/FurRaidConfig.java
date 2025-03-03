/*
---------------------------------------------------------------------------------
File Name : Module

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import eu.fluffici.bot.api.bucket.Validation;
import eu.fluffici.bot.api.bucket.ValidationType;
import lombok.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Data
@ToString
public class FurRaidConfig implements Serializable {
    @Validation(type = ValidationType.OBJECT)
    private Settings settings;
    @Validation(type = ValidationType.OBJECT)
    private Features features;

    @Data
    public abstract static class Feature implements Serializable {
        @Validation(type = ValidationType.BOOLEAN)
        private boolean enabled;
    }

    @Data
    public static class Settings implements Serializable {
        @Validation(type = ValidationType.ARRAY)
        private List<String> staffRoles;

        @Validation(type = ValidationType.ARRAY)
        private List<String> exemptedChannels;

        @Validation(type = ValidationType.ARRAY)
        private List<String> exemptedRoles;

        @Validation(type = ValidationType.ARRAY)
        private List<String> disabledCommands;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean whitelistOverride;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean isUsingGlobalBlacklist;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean isUsingLocalBlacklist;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean isUsingJoinLeaveInformation;

        @Validation(type = ValidationType.STRING, required = true, minLength = 2, maxLength = 4)
        private String language;
    }

    @Data
    public static class Features implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private TicketFeature ticket;
        @Validation(type = ValidationType.OBJECT)
        private VerificationFeature verification;
        @Validation(type = ValidationType.OBJECT)
        private AntiRaidFeature antiRaid;
        @Validation(type = ValidationType.OBJECT)
        private InviteTrackerFeature inviteTracker;
        @Validation(type = ValidationType.OBJECT)
        private BlacklistFeature globalBlacklist;
        @Validation(type = ValidationType.OBJECT)
        private BlacklistFeature localBlacklist;
        @Validation(type = ValidationType.OBJECT)
        private WelcomingFeature welcoming;
        @Validation(type = ValidationType.OBJECT)
        private AutoModerationFeature autoModeration;
        @Validation(type = ValidationType.OBJECT)
        private AntiScamFeature antiScamFeature;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TicketFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private TicketSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class TicketSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String categoryId;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String closingCategoryId;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String ticketLoggingChannel;

        @Validation(type = ValidationType.STRING, maxLength = 100)
        private String initialTitle;

        @Validation(type = ValidationType.STRING, maxLength = 4000)
        private String initialMessage;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean transcript;

        @Validation(type = ValidationType.BOOLEAN)
        private boolean autoCloseOnUserLeave;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class VerificationFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private VerificationSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class VerificationSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String verificationGate;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String verificationLoggingChannel;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String verifiedRole;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String unverifiedRole;

        @Validation(type = ValidationType.STRING, maxLength = 4000)
        private String description;

        @Validation(type = ValidationType.ARRAY, maxLength = 5)
        private List<Question> questions;
    }

    @Data
    public static class Question implements Serializable {
        @Validation(type = ValidationType.STRING, minLength = 1, maxLength = 45)
        private String title;

        @Validation(type = ValidationType.STRING, minLength = 1, maxLength = 400)
        private String placeholder;

        @Validation(type = ValidationType.INTEGER, minLength = 1, maxLength = 4000)
        private int min;

        @Validation(type = ValidationType.INTEGER, minLength = 1, maxLength = 4000)
        private int max;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AntiRaidFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private AntiRaidSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class AntiRaidSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String loggingChannel;

        @Validation(type = ValidationType.STRING, required = true)
        private String sensitivity;

        @Validation(type = ValidationType.INTEGER, minLength = 1)
        private int joinThreshold;

        @Validation(type = ValidationType.INTEGER, minLength = 5000)
        private int joinTimeThreshold;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AntiScamFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private AntiScamSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class AntiScamSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String loggingChannel;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String quarantinedRole;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class InviteTrackerFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private InviteTrackerSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class InviteTrackerSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String trackingChannel;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BlacklistFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private BlacklistSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class BlacklistSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String loggingChannel;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WelcomingFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private WelcomingSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class WelcomingSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String welcomeChannel;

        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String goodbyeChannel;

        @Validation(type = ValidationType.STRING, maxLength = 4000)
        private String joinMessage;

        @Validation(type = ValidationType.STRING, maxLength = 4000)
        private String leftMessage;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AutoModerationFeature extends Feature implements Serializable {
        @Validation(type = ValidationType.OBJECT)
        private AutoModerationSettings settings;
    }

    @Data
    @AllArgsConstructor
    public static class AutoModerationSettings implements Serializable {
        @Validation(type = ValidationType.STRING, maxLength = 20, regex = "^[0-9]+$")
        private String loggingChannel;

        @Validation(type = ValidationType.ARRAY, maxLength = 6)
        private List<Module> modules;
    }

    @Data
    public static class Module implements Serializable {
        @Validation(type = ValidationType.BOOLEAN)
        private boolean enabled;

        @Validation(type = ValidationType.STRING, maxLength = 16)
        private String slug;

        @Validation(type = ValidationType.STRING, maxLength = 16)
        private String sensitivity;

        @Validation(type = ValidationType.OBJECT, instancedOf = ModuleCustomSettings.class)
        private ModuleCustomSettings customSettings;
    }

    @Data
    public static class ModuleCustomSettings implements Serializable {
        @Validation(type = ValidationType.INTEGER, minLength = 3)
        private int threshold;

        @Validation(type = ValidationType.INTEGER, minLength = 5000)
        private int timeThreshold;
    }
}