package eu.fluffici.furraid.components.commands.staff;

import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static eu.fluffici.furraid.server.users.FGetUserRoute.fetchUser;

public class CommandUserFlags extends FCommand {
    public CommandUserFlags() {
        super("user-flags", "Get all the flags on a specific user.", CommandCategory.STAFF);
        this.getOptions().put("isStaff", true);
        this.getOptionData().add(new OptionData(OptionType.STRING, "user-id", "Specify the user-id", true));
    }

    @Override
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        String userId = interaction.getOption("user-id").getAsString();
        User user = interaction.getJDA().getUserById(userId);

        int SPAMMER_MASK = 1 << 20;

        if (user != null) {
            DiscordUser fetchUser = fetchUser(user);
            interaction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                            .setTitle(user.getGlobalName() + "'s flags")
                            .setDescription(
                                    """
                                    Fetched from: `user.getFlagsRaw()`
                                    Result:
                                    %s
                                    
                                    ====================================================
                                    Fetched from `https://discord.com/api/users/`
                                    Result:
                                    
                                    %s
                                    
                                    %s
                                    """.formatted(user.getFlagsRaw(), fetchUser.getFlags(), ((user.getFlagsRaw() & SPAMMER_MASK) == SPAMMER_MASK)))
                    .build()
            ).queue();
        } else {
            interaction.replyEmbeds(this.buildError("This user doesn't exist")).queue();
        }
    }
}
