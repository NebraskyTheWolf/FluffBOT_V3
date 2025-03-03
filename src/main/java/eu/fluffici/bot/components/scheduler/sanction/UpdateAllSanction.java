package eu.fluffici.bot.components.scheduler.sanction;

/*
---------------------------------------------------------------------------------
File Name : UpdateAllSanction.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.PermanentRole;
import eu.fluffici.bot.api.interactions.Task;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class UpdateAllSanction extends Task {
    private final FluffBOT instance;
    public UpdateAllSanction(FluffBOT instance) {
        this.instance = instance;
        this.instance.getLogger().debug("Loading 'UpdateAllSanction' scheduler.");
    }

    @Override
    public void execute() {
        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));
        Role firstWarn = guild.getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.one"));
        Role secondWarn = guild.getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.two"));
        Role thirdWarn = guild.getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.three"));

        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            CompletableFuture.runAsync(() -> {
                try {
                    this.instance.getGameServiceManager().updateAllSanctions().forEach(sanctionBean -> {
                        switch (sanctionBean.getTypeId()) {
                            case 1 -> {
                               try {
                                   Member member = guild.getMemberById(sanctionBean.getUserId());
                                   int warns = this.instance.getGameServiceManager().getAllActiveWarns(sanctionBean.getUserId()).size();

                                   boolean hasFirstWarn = member.getRoles().contains(firstWarn);
                                   boolean hasSecondWarn = member.getRoles().contains(secondWarn);
                                   boolean hasThirdWarn = member.getRoles().contains(thirdWarn);

                                   guild.modifyMemberRoles(member, null, listOf(firstWarn, secondWarn, thirdWarn)).queue();

                                   this.instance.getGameServiceManager()
                                           .fetchPermanentRoles(member)
                                           .forEach(role -> this.instance.getGameServiceManager().removePermanentRole(role));

                                   if (warns == 1) {
                                       guild.addRoleToMember(member, firstWarn).queue();
                                       this.instance.getGameServiceManager().addPermanentRole(new PermanentRole(
                                               member,
                                               firstWarn.getId()
                                       ));
                                   }
                                   else if (warns == 2) {
                                       guild.addRoleToMember(member, secondWarn).queue();
                                       this.instance.getGameServiceManager().addPermanentRole(new PermanentRole(
                                               member,
                                               secondWarn.getId()
                                       ));
                                   } else if (warns >= 3) {
                                       guild.addRoleToMember(member, thirdWarn).queue();
                                       this.instance.getGameServiceManager().addPermanentRole(new PermanentRole(
                                               member,
                                               thirdWarn.getId()
                                       ));
                                   }
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                            }
                            case 2 -> guild.unban(UserSnowflake.fromId(sanctionBean.getUserId())).queue();
                            case 4 -> guild.removeTimeout(UserSnowflake.fromId(sanctionBean.getUserId())).queue();
                        }
                    });
                } catch (Exception e) {
                    this.instance.getLogger().error("Error while updating the sanctions.", e);
                    e.printStackTrace();
                }
            });
        }, 1, 10, TimeUnit.SECONDS);
    }
}
