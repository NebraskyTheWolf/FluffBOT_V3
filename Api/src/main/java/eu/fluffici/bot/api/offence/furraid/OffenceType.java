package eu.fluffici.bot.api.offence.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

/**
 * OffenceType is an enum class representing different types of offences with their associated offence details.
 * OffenceType contains a list of OffenceDetails for each offence type.
 */
@Getter
public enum OffenceType {
    LOW(
            listOf(
                    new OffenceDetails("mass_mentions", listOf(ActionType.BLOCK, ActionType.ALERT), 10, 15000, 3, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("spam", listOf(ActionType.BLOCK, ActionType.ALERT), 20, 15000, 3, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("repeated_messages", listOf(ActionType.BLOCK), 5, 15000, 3, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("link_protection", listOf(ActionType.BLOCK), 1, 15000, 3, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("emoji_spam", listOf(ActionType.BLOCK), 5, 15000, 3, listOf(ActionType.TIMEOUT)),
                    new OffenceDetails("attachment_spam", listOf(ActionType.BLOCK), 5, 15000, 3, listOf(ActionType.TIMEOUT))
            )
    ),
    MEDIUM(
            listOf(
                    new OffenceDetails("mass_mentions", listOf(ActionType.BLOCK, ActionType.ALERT), 5, 10000, 2, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("spam", listOf(ActionType.BLOCK, ActionType.ALERT), 15, 10000, 2, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("repeated_messages", listOf(ActionType.BLOCK, ActionType.ALERT), 5, 10000, 2, listOf(ActionType.TIMEOUT, ActionType.SOFT_WARN)),
                    new OffenceDetails("link_protection", listOf(ActionType.BLOCK, ActionType.ALERT), 1, 10000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("emoji_spam", listOf(ActionType.BLOCK), 5, 10000, 2, listOf(ActionType.TIMEOUT)),
                    new OffenceDetails("attachment_spam", listOf(ActionType.BLOCK), 5, 10000, 2, listOf(ActionType.TIMEOUT))
            )
    ),
    HIGH(
            listOf(
                    new OffenceDetails("mass_mentions", listOf(ActionType.BLOCK, ActionType.ALERT), 4, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("spam", listOf(ActionType.BLOCK, ActionType.ALERT), 10, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("repeated_messages", listOf(ActionType.BLOCK, ActionType.ALERT), 4, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("link_protection", listOf(ActionType.BLOCK, ActionType.ALERT), 1, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("emoji_spam", listOf(ActionType.BLOCK, ActionType.ALERT), 4, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("attachment_spam", listOf(ActionType.BLOCK, ActionType.ALERT), 5, 7000, 2, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN))
            )
    ),
    STRICT(
            listOf(
                    new OffenceDetails("mass_mentions", listOf(ActionType.BLOCK, ActionType.ALERT), 2, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("spam", listOf(ActionType.BLOCK, ActionType.ALERT), 5, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("repeated_messages", listOf(ActionType.BLOCK, ActionType.ALERT), 2, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("link_protection", listOf(ActionType.BLOCK, ActionType.ALERT), 1, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("emoji_spam", listOf(ActionType.BLOCK, ActionType.ALERT), 3, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN)),
                    new OffenceDetails("attachment_spam", listOf(ActionType.BLOCK, ActionType.ALERT), 3, 5000, 1, listOf(ActionType.TIMEOUT, ActionType.HARD_WARN))
            )
    );

    /**
     * Represents a list of OffenceDetails associated with an OffenceType.
     */
    private final List<OffenceDetails> offences;

    /**
     * Represents an offence type with its corresponding offence details.
     *
     * @param offences The list of OffenceDetails associated with this offence type.
     */
    OffenceType(List<OffenceDetails> offences) {
        this.offences = offences;
    }

    /**
     * Determines if the specified offence detail exists in the collection of offences.
     *
     * @param slug the name of the offence detail to search for
     * @return true if the offence detail exists, false otherwise
     */
    public boolean hasOffenceDetail(String slug) {
        for (OffenceDetails offence : offences)
            if (offence.getName().equals(slug))
                return true;
        return false;
    }

    /**
     * Retrieves the type of offence based on the given slug.
     *
     * @param slug the slug representing the name of the offence
     * @return the OffenceDetails object that matches the given slug, or null if not found
     */
    @Nullable
    public OffenceDetails getOffenceType(String slug) {
        for (OffenceDetails offence : offences)
            if (offence.getName().equals(slug))
                return offence;
        return null;
    }

    @Getter
    @AllArgsConstructor
    public static class OffenceDetails {
        private final String name;
        private final List<ActionType> actionTypes;
        private final int threshold;
        private final int timeThreshold;
        private final int strikeThreshold;
        private final List<ActionType> strikesAction;
    }
}