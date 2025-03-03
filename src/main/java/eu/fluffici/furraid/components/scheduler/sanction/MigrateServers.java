package eu.fluffici.furraid.components.scheduler.sanction;

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


import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MigrateServers extends Task {
    private final FurRaidDB instance;

    public MigrateServers(FurRaidDB instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading UpdateAllSanction scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> CompletableFuture.runAsync(() -> {
            try {
                for (Guild guild : this.instance.getJda().getGuilds()) {
                    if (instance.getGameServiceManager().fetchGuildSettings(guild) == null) {
                        instance.getGameServiceManager().createGuild(new GuildSettings(
                                guild.getId(),
                                null,
                                null,
                                false,
                                null,
                                null
                        ));
                    } else {
                        guild.updateCommands().queue();
                    }
                }
            } catch (Exception e) {
                this.instance.getLogger().error("Error while updating the sanctions.", e);
                e.printStackTrace();
            }
        }), 1, 10, TimeUnit.SECONDS);
    }
}
