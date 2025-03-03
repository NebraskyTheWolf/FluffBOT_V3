/*
---------------------------------------------------------------------------------
File Name : RaidOffenceType

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 09/07/2024
Last Modified : 09/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.offence.furraid;

import lombok.Getter;

import java.util.List;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@Getter
public enum RaidOffenceType {
    LOW(
            listOf(
                    new OffenceDetails("JOIN_SPAM", 10, 30000),
                    new OffenceDetails("MESSAGE_SPAM", 20, 30000),
                    new OffenceDetails("MASS_MENTION", 5, 30000)
            )
    ),
    MEDIUM(
            listOf(
                    new OffenceDetails("JOIN_SPAM", 7, 20000),
                    new OffenceDetails("MESSAGE_SPAM", 15, 20000),
                    new OffenceDetails("MASS_MENTION", 3, 20000)
            )
    ),
    HIGH(
            listOf(
                    new OffenceDetails("JOIN_SPAM", 5, 10000),
                    new OffenceDetails("MESSAGE_SPAM", 10, 10000),
                    new OffenceDetails("MASS_MENTION", 2, 10000)
            )
    ),
    STRICT(
            listOf(
                    new OffenceDetails("JOIN_SPAM", 3, 5000),
                    new OffenceDetails("MESSAGE_SPAM", 5, 5000),
                    new OffenceDetails("MASS_MENTION", 1, 5000)
            )
    );

    private final List<OffenceDetails> offences;

    RaidOffenceType(List<OffenceDetails> offences) {
        this.offences = offences;
    }

    @Getter
    public static class OffenceDetails {
        private final String name;
        private final int threshold;
        private final int timeThreshold;

        public OffenceDetails(String name, int threshold, int timeThreshold) {
            this.name = name;
            this.threshold = threshold;
            this.timeThreshold = timeThreshold;
        }
    }
}
