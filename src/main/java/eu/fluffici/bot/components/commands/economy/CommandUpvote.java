package eu.fluffici.bot.components.commands.economy;

/*
---------------------------------------------------------------------------------
File Name : CommandUpvote.java

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
import eu.fluffici.bot.api.CurrencyType;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.time.Duration;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_UPVOTE;

@CommandHandle
public class CommandUpvote extends Command {

    public CommandUpvote() {
        super("upvote", "Upvote someone", CommandCategory.ECONOMY);

        this.getOptions().put("channelRestricted", true);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select the user to upvote", true));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        PlayerBean target = this.getUserManager().fetchUser(user);
        PlayerBean author = this.getUserManager().fetchUser(interaction.getUser());

        Pair<Boolean, Duration> limited = this.isRateLimited(interaction.getUser(), 61);

        if (limited.getLeft()) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.upvote.error", limited.getRight().toHours())
            )).setEphemeral(true).queue();
            return;
        }

        if (target == null) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("common.user_not_found")
            )).setEphemeral(true).queue();
            return;
        }
        if (author == null) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("common.profile_not_found")
            )).setEphemeral(true).queue();
            return;
        }

        if (!this.getUserManager().hasEnoughUpvotes(author, 1)) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.upvote.error.not_enough")
            )).setEphemeral(true).queue();
            return;
        }

        Pair<Boolean, String> result = this.getUserManager().transferTokens(author, CurrencyType.UPVOTE, 1, target);
        if (!result.getLeft()) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.upvote.error", result.getRight())
            )).setEphemeral(true).queue();
            return;
        }

        if (interaction.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = (TextChannel) interaction.getChannel();
            if (channel != null) {
                channel.sendMessageEmbeds(this.getEmbed()
                        .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("command.upvote.title", interaction.getUser().getGlobalName(), user.getGlobalName()), "https://fluffici.eu", ICON_UPVOTE)
                                .setThumbnail(user.getAvatarUrl())
                                .setTimestamp(Instant.now())
                        .build()
                ).setContent(user.getAsMention()).queue();
            }
        }

        // Logging to save traces.
        this.getLogger().info("%s up-voted %s", author.getUserId(), target.getUserId());

        FluffBOT.getInstance().getAchievementManager().unlock(interaction.getUser(), 36);

        interaction.replyEmbeds(this.buildSuccess(
                this.getLanguageManager().get("command.upvote.success", user.getAsMention())
        )).setEphemeral(true).queue();
    }
}
