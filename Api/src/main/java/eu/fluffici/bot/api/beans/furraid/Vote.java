package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Vote {
    private UserSnowflake user;
    private Timestamp createdAt;
}
