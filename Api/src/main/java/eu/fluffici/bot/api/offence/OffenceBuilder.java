/*
---------------------------------------------------------------------------------
File Name : OffenceBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 13/06/2024
Last Modified : 13/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.offence;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * OffenceBuilder is a class that represents an offense builder.
 * It is used to track offenses and check if the threshold for the associated offense type has been reached.
 */
@Getter
@Setter
@Builder
public class OffenceBuilder {
    private User user;
    private OffenceType offenceType;
    private Channel channel;
    private AtomicInteger offenceCount = new AtomicInteger();

    /**
     * Checks if the offence count has reached the threshold for the associated offence type.
     *
     * @return true if the offence count is greater than or equal to the threshold, false otherwise.
     */
    public boolean isThresholdReached() { return this.offenceCount.get() >= this.offenceType.getThreshold(); }

    /**
     * Increments the offence count for the given OffenceBuilder object.
     * The offence count represents the number of offenses that have occurred.
     * This method increments the offence count by 1 using an atomic operation.
     */
    public void incrementOffenceCount() { this.offenceCount.incrementAndGet(); }
}