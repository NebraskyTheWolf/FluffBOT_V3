package eu.fluffici.bot.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildAddListener.java

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
import eu.fluffici.bot.api.interactions.Interactions;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("All")
public class GuildAddListener extends ListenerAdapter {

    private final FluffBOT instance;

    public GuildAddListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onGuildJoin(@NonNull GuildJoinEvent event) {
        User self = this.instance.getJda().getSelfUser();
        Guild guild = event.getGuild();

        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(guild.getId())) {
            if (guild.getSystemChannel() != null && guild.getSystemChannel().canTalk(event.getGuild().getSelfMember())) {
                guild.getSystemChannel().sendMessageEmbeds(this.instance.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor("Not allowed.", "https://fluffici.eu", ICON_HEXAGON_CIRCLE)
                        .setDescription("You are not allowed to invite the bot in this discord server. \n For more information please contact `administrace@fluffici.eu`.")
                        .setTimestamp(Instant.now())
                        .setFooter(self.getGlobalName(), self.getAvatarUrl())
                        .build()
                ).setContent(event.getGuild().getOwner().getAsMention()).queue();
            } else {
                this.instance.getLogger().warn("Cannot talk in %s channel for guild %s", guild.getSystemChannel().getId(), guild.getId());
            }

            guild.leave().queue();

            this.instance.getLogger().warn("%s guild tried to invite to bot on a non-allowed discord server.", guild.getId());
            this.instance.getLogger().warn("Leaving %s guild due to non-allowed guild.", guild.getId());
        } else {
            handleMigration(guild, event.getGuild().getOwner());
        }
    }

    /**
     * Handles the migration process for a guild.
     *
     * @param guild The guild to handle the migration for.
     * @param owner The owner of the guild.
     */
    public static void handleMigration(@NotNull Guild guild, Member owner) {
        if (guild.getSystemChannel() != null && guild.getSystemChannel().canTalk(guild.getSelfMember())) {
            Pair<Button, String> interaction = FluffBOT.getInstance().getButtonManager().toInteraction("migrate:guild", owner, guild.getSystemChannel(), false);

            Message message = guild.getSystemChannel().sendMessageEmbeds(FluffBOT.getInstance().getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor("Server configuration adjustment.", "https://fluffici.eu", ICON_SETTINGS)
                    .setTitle("Please confirm this operation.")
                    .setTimestamp(Instant.now())
                    .setDescription("- Synchronising the member list * \n - Checking channel check-list. \n - Validating configuration \n - Setting up missing channels. \n\n Please click on the button 'Migrate' bellow to validate the server configuration.")
                    .setFooter("This operation should take less than 3 minutes.", ICON_QUESTION_MARK)
                    .build()
            ).addActionRow(interaction.getLeft()).setContent(owner.getAsMention()).complete();

            Interactions inter = FluffBOT.getInstance().getInteractionManager().fetchInteraction(interaction.getRight());
            inter.setMessageId(message.getId());
            inter.setAttached(true);

            FluffBOT.getInstance().getInteractionManager().updateInteraction(inter);
        } else {
            FluffBOT.getInstance().getLogger().warn("Cannot talk in %s channel for guild %s", guild.getSystemChannel().getId(), guild.getId());
        }

        FluffBOT.getInstance().getLogger().warn("%s Main guild detected, setting up pre-configuration...", guild.getId());
    }
}
