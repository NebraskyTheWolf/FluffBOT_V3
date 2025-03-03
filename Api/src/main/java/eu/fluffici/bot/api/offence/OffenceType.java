/*
---------------------------------------------------------------------------------
File Name : OffenceType

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 13/06/2024
Last Modified : 13/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.offence;

import lombok.Getter;
import java.util.List;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

/**
 * OffenceType is an enumeration representing different types of offenses.
 * Each offense type has a list of ActionTypes, a threshold value, and a time threshold value.
 * OffenceType is used to classify certain actions as offenses based on the configured criteria.
 */
@Getter
public enum OffenceType {
    NONE(listOf(), -1, -1, 0, listOf()),
    SPAM(listOf(ActionType.BLOCK, ActionType.ALERT), 15, 10000, 2, listOf(ActionType.TIMEOUT)),
    MALICIOUS_LINK(listOf(ActionType.BLOCK, ActionType.ALERT), 1, 10000, 2, listOf(ActionType.TIMEOUT)),
    SCAM_BOT(listOf(ActionType.BLOCK), 2, 10000, 2, listOf(ActionType.TIMEOUT));

    /**
     * Represents a list of ActionTypes associated with an OffenceType.
     */
    private final List<ActionType> actionTypes;
    private final List<ActionType> strikesAction;

    /**
     * Represents the threshold value for an offense type.
     * The threshold determines the limit at which certain actions are classified as offenses.
     */
    private final int threshold;

    /**
     * Represents the time threshold value for an offense type.
     * The time threshold determines the duration within which certain actions are classified as offenses.
     */
    private final int timeThreshold;
    private final int strikeThreshold;

    /**
     * Represents an offense type with its corresponding action types, threshold, and time threshold.
     * An offense type is used to classify certain actions as offenses based on the configured criteria.
     *
     * @param actionTypes The list of ActionTypes associated with this offense type.
     * @param threshold The threshold value for this offense type.
     * @param timeThreshold The time threshold value for this offense type.
     */
    OffenceType(List<ActionType> actionTypes, int threshold, int timeThreshold, int strikeThreshold, List<ActionType> strikesAction) {
        this.actionTypes = actionTypes;
        this.threshold = threshold;
        this.timeThreshold = timeThreshold;
        this.strikeThreshold = strikeThreshold;
        this.strikesAction = strikesAction;
    }
}
