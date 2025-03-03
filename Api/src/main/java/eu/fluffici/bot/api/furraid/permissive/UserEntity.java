package eu.fluffici.bot.api.furraid.permissive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Getter
    private String token;
    protected int permissions;
}
