package eu.fluffici.furraid.components.scheduler.verification;

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import java.util.concurrent.TimeUnit;

public class VerificationSyncTask extends Task {
    private final FurRaidDB instance;
    public VerificationSyncTask(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> this.instance.getJda().getGuilds().forEach(guild -> {
            GuildSettings guildSettings = this.instance.getBlacklistManager().fetchGuildSettings(guild);
            if (guildSettings != null && !guildSettings.isBlacklisted()) {
                FurRaidConfig.VerificationFeature verificationFeature = guildSettings.getConfig().getFeatures().getVerification();

                if (verificationFeature.isEnabled() && verificationFeature.getSettings().getVerificationLoggingChannel() != null) {
                    this.instance.getGameServiceManager().getAllVerificationRecords(guild.getId()).forEach(verification -> {
                        TextChannel gateChannel = guild.getTextChannelById(verificationFeature.getSettings().getVerificationLoggingChannel());

                        if (gateChannel != null) {
                            Message originalMessage = gateChannel.retrieveMessageById(verification.getMessageId()).complete();
                            Member verifiedBy = guild.getMemberById(verification.getVerifiedBy());

                            if (originalMessage != null && verifiedBy != null) {
                                for (Component component : originalMessage.getComponents()) {
                                    if (component.getType() == Component.Type.STRING_SELECT) {
                                        switch (verification.getStatus()) {
                                            case "VERIFIED", "ACCEPTED" -> originalMessage.editMessageComponents(ActionRow.of(
                                                    Button.success("button:none", "Verified By " + verifiedBy.getUser().getGlobalName()).asDisabled())).queue();
                                            case "DENIED" -> originalMessage.editMessageComponents(ActionRow.of(
                                                    Button.danger("button:none", "Verification denied by " + verifiedBy.getUser().getGlobalName()).asDisabled())).queue();
                                        }

                                        this.instance.getLogger().warn("[SYNC] Correcting components for verification [%s] due to a faulty-sync on guild [%s] and message [%s]", verification.getVerificationCode(), guild.getId(), originalMessage.getId());
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }), 5, 10, TimeUnit.SECONDS);
    }
}
