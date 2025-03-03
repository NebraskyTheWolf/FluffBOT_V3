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


import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.UserSnowflake;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UpdateAllSanction extends Task {
    private final FurRaidDB instance;

    public UpdateAllSanction(FurRaidDB instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading UpdateAllSanction scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> CompletableFuture.runAsync(() -> {
            try {
                this.instance.getGameServiceManager().updateAllSanctionsFrdb().forEach(sanction -> {
                    switch (sanction.getTypeId()) {
                        case 2 -> Objects.requireNonNull(this.instance.getJda().getGuildById(sanction.getGuildId()))
                                .unban(UserSnowflake.fromId(sanction.getUserId()))
                                .queue();
                        case 4 -> Objects.requireNonNull(this.instance.getJda().getGuildById(sanction.getGuildId()))
                                .removeTimeout(UserSnowflake.fromId(sanction.getUserId()))
                                .queue();
                    }
                });
            } catch (Exception e) {
                this.instance.getLogger().error("Error while updating the sanctions.", e);
                e.printStackTrace();
            }
        }), 5, 10, TimeUnit.SECONDS);
    }
}
