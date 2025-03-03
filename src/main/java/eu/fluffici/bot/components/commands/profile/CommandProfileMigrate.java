/*
---------------------------------------------------------------------------------
File Name : CommandRoles

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.profile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.ProfileMigration;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

public class CommandProfileMigrate extends Command  {

    private final OkHttpClient client = new OkHttpClient();

    public CommandProfileMigrate() {
        super("profile-migrate", "Migrate your profile from V2 to V3", CommandCategory.PROFILE);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull CommandInteraction interaction) {
        User user = interaction.getUser();
        Member member = interaction.getMember();
        PlayerBean player = this.getUserManager().fetchUser(user);

        if (player.getMigrationId() != null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.migrate.already_migrated"))).queue();
            return;
        }

        Request request = new Request.Builder()
                .url("https://api.fluffici.eu/api/discord/profile/".concat(user.getId()))
                .header("Authorization", "Bearer ".concat(FluffBOT.getInstance().getDefaultConfig().getProperty("api.fluffici.token")))
                .get()
                .build();

        ProfileMigration profileMigration;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                if (data.has("status") && data.get("status").getAsBoolean()) {
                    profileMigration = new Gson().fromJson(data.get("data").getAsJsonObject().toString(), ProfileMigration.class);
                } else {
                    profileMigration = null;
                }
            } else {
                profileMigration = null;
            }
        } catch (Exception e) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.migrate.error"))).queue();
            return;
        }

        if (profileMigration == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.migrate.unavailable"))).queue();
            return;
        }

        player.setMigrationId(profileMigration.get_id());
        player.setLegacyMessageCount(profileMigration.getMessages());
        player.setSeed(new Random().nextInt(-5000000, 5000000));

        FluffBOT.getInstance().getUserManager().addUpvote(player, profileMigration.getUpvote());
        FluffBOT.getInstance().getUserManager().addEvent(player, profileMigration.getEvents());
        FluffBOT.getInstance().getAchievementManager().unlock(user, 33);

        this.getUserManager().addItem(interaction.getUser(), "nickname_coin", 2);
        this.getUserManager().addTokens(player, 200);

        if (member.isBoosting()) {
            FluffBOT.getInstance().getAchievementManager().unlock(member, 34);
        }

        FluffBOT.getInstance().getAchievementManager().incrementAchievements(member.getId(), new int[]{ 1, 2, 3, 4, 5 }, profileMigration.getMessages());

        this.getUserManager().saveUser(player);

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.migrate.completed"))).queue();
    }
}