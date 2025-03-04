/*
---------------------------------------------------------------------------------
File Name : CommandData

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/07/2024
Last Modified : 15/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.profile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.ReentrantReadWriteLockAdapter;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.GsonBuilder;

public class CommandData extends Command {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ReentrantReadWriteLock.class, new ReentrantReadWriteLockAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public CommandData() {
        super("data", "Get all your stored data in FluffBOT!", CommandCategory.PROFILE);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        try {
            JsonObject result = new JsonObject();
            PlayerBean playerBean = this.getUserManager().fetchUser(interaction.getUser());

            interaction.deferReply(true).queue();

            String requestId =  GameId.generateId();

            result.addProperty("id", interaction.getUser().getId());
            result.addProperty("username", interaction.getUser().getName());
            result.addProperty("requestId", requestId);

            this.addNonNullProperty(result, "player_data", playerBean);
            this.addNonNullProperty(result, "player_clan_data", this.getUserManager().fetchClan(playerBean));
            this.addNonNullProperty(result, "player_birth_data", this.getUserManager().fetchBirthdate(interaction.getUser()));
            this.addNonNullProperty(result, "player_channel_data", FluffBOT.getInstance().getGameServiceManager().getChannel(interaction.getUser()));
            this.addNonNullProperty(result, "player_inventory_data", FluffBOT.getInstance().getGameServiceManager().fetchInformation(interaction.getUser()));

            PlayerBeanDTO playerBeanDTO = new PlayerBeanDTO(result);

            CompletableFuture<ByteArrayOutputStream> future = CompletableFuture.supplyAsync(() -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                gson.toJson(playerBeanDTO, writer);
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return outputStream;
            });
            future.whenComplete((baos, throwable) -> interaction.getUser().openPrivateChannel().queue(channel -> {
                if (channel.canTalk()) {
                    channel.sendMessageEmbeds(this.buildSuccess("There is your user-data collected from FluffBOT!"))
                            .addFiles(FileUpload.fromData(baos.toByteArray(), "data_".concat(interaction.getUser().getId()).concat(".json")))
                            .setNonce(requestId)
                            .setMessageReference(interaction.getMessageChannel().getLatestMessageId())
                            .queue(
                                    success -> {
                                        if (Objects.equals(success.getNonce(), requestId)) {
                                            TextChannel loggingChannel = interaction.getGuild().getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.logging"));
                                            loggingChannel.sendMessageEmbeds(this.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                            .setAuthor(interaction.getUser().getEffectiveName(), null, interaction.getUser().getEffectiveAvatarUrl())
                                                            .setTitle("User data collection completed from FluffBOT")
                                                            .addField("Nonce", success.getNonce(), false)
                                                            .addField("Nonce integrity", "Validated", true)
                                                            .setTimestamp(Instant.now())
                                                    .build()
                                            ).queue();
                                        }
                                    }
                            );

                    interaction.getHook().sendMessageEmbeds(this.buildSuccess("Your data has been exported successfully, Please read your DM")).setEphemeral(true).queue();
                } else {
                    interaction.getHook().sendMessageEmbeds(this.buildError("Please enable your Private Message to receive your data.")).setEphemeral(true).queue();
                }
            }));
            future.exceptionally(throwable -> {
                throwable.printStackTrace();
                interaction.getHook().sendMessageEmbeds(this.buildError("Something went wrong while exporting your data.")).setEphemeral(true).queue();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNonNullProperty(JsonObject result, String propertyName, Object value) {
        if (value != null) {
            result.add(propertyName, this.gson.fromJson(this.gson.toJson(value), JsonElement.class));
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlayerBeanDTO {
        private JsonObject data;
    }
}