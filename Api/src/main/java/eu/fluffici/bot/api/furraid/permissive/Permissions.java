package eu.fluffici.bot.api.furraid.permissive;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("All")
public class Permissions {
    public static final int READ_VOTE   = 1 << 1;
    public static final int WRITE_VOTE   = 1 << 2;
    public static final int ADMIN = 1 << 3;
    public static final int GET_SERVERS = 1 << 4;
    public static final int PATCH_SERVERS = 1 << 5;
    public static final int CHECK_STAFF = 1 << 6;
    public static final int CHECK_PREMIUM = 1 << 7;
    public static final int GUILD_MANAGEMENT = 1 << 8;
    public static final int BLACKLIST_READ = 1 << 9;
    public static final int SEND_MESSAGE = 1 << 10;
    public static final int GET_USER_INFO = 1 << 11;

    private UserEntity userEntity;

    public Permissions(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Contract(pure = true)
    public static int calculatePermissions(@NotNull int... actions) {
        int result = 0;
        for (int action : actions) {
            result |= action;
        }
        return result;
    }

    public boolean hasPermission(int permission) {
        return (this.userEntity.permissions & permission) != 0;
    }
}
