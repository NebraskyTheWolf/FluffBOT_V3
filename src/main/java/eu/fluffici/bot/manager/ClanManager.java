package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : ClanManager.java

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
import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.clans.ClanRequestBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.beans.players.UserClanBean;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.Collections;
import java.util.List;

public class ClanManager {

    private final FluffBOT fluffbot;

    public ClanManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    public ClanBean fetchClan(PlayerBean player) {
        try {
            UserClanBean currentClan = this.fluffbot.getUserManager().fetchClan(player);
            if (currentClan != null) {
                return this.fluffbot.getGameServiceManager().fetchClan(currentClan.getClanId());
            }
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to fetch '" + player.getUserId() + "' clan.", e);
            e.printStackTrace();
        }
        return null;
    }

    public ClanMembersBean fetchClanMember(PlayerBean player) {
        try {
            ClanMembersBean currentClan = this.fluffbot.getGameServiceManager().fetchClanMember(player);
            if (currentClan != null) {
                return currentClan;
            }
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to fetch '" + player.getUserId() + "' member clan.", e);
            e.printStackTrace();
        }
        return null;
    }

    public ClanRequestBean fetchUserInvite(PlayerBean player) {
        try {
            return this.fluffbot.getGameServiceManager().getActiveRequest(player.getUserId());
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to fetch user invite.", e);
            e.printStackTrace();
        }
        return null;
    }

    public void updateClan(ClanBean clan) {
        try {
            this.fluffbot.getGameServiceManager().updateClan(clan);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to update clan.", e);
            e.printStackTrace();
        }
    }

    public void deleteClanMember(ClanMembersBean clan) {
        try {
            this.fluffbot.getGameServiceManager().deleteClanMember(clan);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to delete clan member.", e);
            e.printStackTrace();
        }
    }

    public void createClan(ClanBean clan) {
        try {
            this.fluffbot.getGameServiceManager().createClan(clan);
            this.fluffbot.getAchievementManager().unlock(UserSnowflake.fromId(clan.getOwnerId()), 38);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to create clan.", e);
            e.printStackTrace();
        }
    }

    public void addClanMember(ClanMembersBean clan) {
        try {
            this.fluffbot.getGameServiceManager().addClanMember(clan);
            this.fluffbot.getAchievementManager().unlock(UserSnowflake.fromId(clan.getUserId()), 43);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to add clan member.", e);
            e.printStackTrace();
        }
    }

    public List<ClanMembersBean> fetchClanMembers(ClanBean clan) {
        try {
            return this.fluffbot.getGameServiceManager().fetchClanMembers(clan.getClanId());
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to fetch clan members.", e);
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public void sendClanInvite(ClanRequestBean request) {
        try {
            this.fluffbot.getGameServiceManager().createClanInvite(request);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to send clan invite.", e);
            e.printStackTrace();
        }
    }

    public void acknowledgeInvite(String inviteId) {
        try {
            this.fluffbot.getGameServiceManager().acknowledgeInvite(inviteId);
        } catch (Exception e) {
            this.fluffbot.getLogger().error("Unable to acknowledge clan invite.", e);
            e.printStackTrace();
        }
    }
}
