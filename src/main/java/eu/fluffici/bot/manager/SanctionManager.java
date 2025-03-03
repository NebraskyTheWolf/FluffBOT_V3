package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : SanctionManager.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.events.UserSanctionEvent;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
public class SanctionManager {

    private final FluffBOT fluffbot;

    public SanctionManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    /**
     * Mutes a user in a guild.
     *
     * @param guild      The guild where the user is being muted.
     * @param user       The user to be muted.
     * @param author     The user who initiated the mute.
     * @param reason     The reason for the mute.
     * @param expiration The expiration time for the mute in milliseconds.
     * @return true if the user was successfully muted, false if the user is already muted.
     */
    @SneakyThrows
    public boolean mute(Guild guild, User user, User author, String reason, Instant expiration, Message.Attachment attachment) {
        SanctionBean mute = this.fluffbot.getGameServiceManager().getPlayerMuted(user.getId());
        if (mute != null) {
            this.fluffbot.getGameServiceManager().updateSanction(mute.getSanctionId(), true);
        }

        SanctionBean sanctionBean = new SanctionBean(
                1L,
                SanctionBean.MUTE,
                user.getId(),
                reason,
                author.getId(),
                new Timestamp(expiration.toEpochMilli()),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                attachment.getUrl()
        );

        FluffBOT.getInstance().getGameServiceManager().applySanction(SanctionBean.MUTE, sanctionBean);

        guild.timeoutUntil(UserSnowflake.fromId(user.getId()), expiration).reason(reason).queue();

        this.fluffbot.getEventBus().post(new UserSanctionEvent(
                this.fluffbot.getUserManager().fetchUserAsync(author),
                sanctionBean,
                this.fluffbot.getUserManager().fetchUserAsync(user),
                attachment,
                true
        ));

        return true;
    }

    /**
     * Warns a user in a guild, applying a warn sanction and logging to warn.
     *
     * @param guild  The guild where the user is being warned.
     * @param user   The user being warned.
     * @param author The user who issued to warn.
     * @param reason The reason for to warn.
     * @return true if to warn was successfully issued, false otherwise.
     */
    @SneakyThrows
    public boolean warn(Guild guild, User user, User author, String reason, Message.Attachment attachment) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Prague"));
        LocalDateTime oneYearFromNow = now.plusYears(1);

        SanctionBean sanctionBean = new SanctionBean(
                1L,
                SanctionBean.WARN,
                user.getId(),
                reason,
                author.getId(),
                Timestamp.valueOf(oneYearFromNow),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                attachment.getUrl()
        );

        FluffBOT.getInstance().getGameServiceManager().applySanction(SanctionBean.WARN, sanctionBean);

        this.fluffbot.getEventBus().post(new UserSanctionEvent(
                this.fluffbot.getUserManager().fetchUserAsync(author),
                sanctionBean,
                this.fluffbot.getUserManager().fetchUserAsync(user),
                attachment,
                true
        ));

        return true;
    }

    /**
     * Bans a user in a guild, applying a ban sanction, logging the ban, and banning the user.
     *
     * @param guild      The guild where the user is being banned.
     * @param user       The user to be banned.
     * @param author     The user who initiated the ban.
     * @param reason     The reason for the ban.
     * @param expiration The expiration time for the ban in milliseconds.
     * @return true if the user was successfully banned, false if the user is already banned.
     */
    @SneakyThrows
    public boolean ban(@NotNull Guild guild, @NotNull User user, @NotNull User author, String reason, long expiration, @Nullable Message.Attachment attachment) {
        SanctionBean sanctionBean = new SanctionBean(
                1L,
                SanctionBean.BAN,
                user.getId(),
                reason,
                author.getId(),
                new Timestamp(expiration),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                (attachment != null ? attachment.getUrl() : "")
        );

        FluffBOT.getInstance().getGameServiceManager().applySanction(SanctionBean.BAN, sanctionBean);

        guild.getMemberById(user.getId()).ban(7, TimeUnit.DAYS).reason(reason).queue();

        this.fluffbot.getEventBus().post(new UserSanctionEvent(
                this.fluffbot.getUserManager().fetchUserAsync(author),
                sanctionBean,
                this.fluffbot.getUserManager().fetchUserAsync(user),
                attachment,
                true
        ));

        return true;
    }

    /**
     * Kicks a user from a guild, applies a kick sanction, and logs the kick.
     *
     * @param user       The member to be kicked.
     * @param author     The user who initiated the kick.
     * @param reason     The reason for the kick.
     * @param attachment The attachment associated with the kick.
     */
    @SneakyThrows
    public void kick(Member user, User author, String reason, @Nullable Message.Attachment attachment) {
        SanctionBean sanctionBean = new SanctionBean(
                1L,
                SanctionBean.KICK,
                user.getId(),
                reason,
                author.getId(),
                null,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                (attachment != null ? attachment.getUrl() : "")
        );

        FluffBOT.getInstance().getGameServiceManager().applySanction(SanctionBean.KICK, sanctionBean);

        user.kick().reason(reason).queue();

        this.fluffbot.getEventBus().post(new UserSanctionEvent(
                this.fluffbot.getUserManager().fetchUserAsync(author),
                sanctionBean,
                this.fluffbot.getUserManager().fetchUserAsync(user),
                attachment,
                attachment != null
        ));
    }

    /**
     * Unbans a user in a guild, removing the ban sanction and logging the unban.
     *
     * @param guild  The guild where the user is being unbanned.
     * @param user   The user to be unbanned.
     * @param author The user who initiated the unban.
     */
    @SneakyThrows
    public void unban(Guild guild, String user, User author) {
        long sanctionId = this.fluffbot.getGameServiceManager().getAllActiveSanctions(user, SanctionBean.BAN).getFirst().getSanctionId();
        this.fluffbot.getGameServiceManager().updateSanction(sanctionId, true);

        guild.unban(UserSnowflake.fromId(user)).reason("Unbanned by " + author.getEffectiveName()).queue();
    }
}
