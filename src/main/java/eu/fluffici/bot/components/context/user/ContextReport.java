package eu.fluffici.bot.components.context.user;

/*
---------------------------------------------------------------------------------
File Name : ContextReport.java

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
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.Context;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

public class ContextReport extends Context<MessageContextInteraction> {
    public ContextReport() {
        super(Command.Type.MESSAGE, "Report Message");
    }

    @Override
    @SneakyThrows
    public void execute(MessageContextInteraction interaction) {
        Message message = interaction.getTarget();

        if (message.getAuthor().isBot()) {
            interaction.reply(getLanguageManager().get("context.report.bot_message")).setEphemeral(true);
            return;
        }

        TextChannel staffChannel = interaction.getGuild()
                .getTextChannelById(FluffBOT.getInstance()
                .getDefaultConfig()
                .getProperty("channel.staff"));

       List<SanctionBean> sanctions = FluffBOT.getInstance()
                .getGameServiceManager()
                .getAllSanctions()
                .stream()
                        .filter(sanctionBean -> sanctionBean.getUserId().equals(message.getAuthor().getId()))
                        .filter(sanctionBean -> sanctionBean.getTypeId() == SanctionBean.WARN)
                        .toList();

       handleConfirmation(interaction,
               this.getLanguageManager().get("context.report.confirm", message.getAuthor().getGlobalName(), message.getContentDisplay()),
               this.getLanguageManager().get("context.report.confirm.button"),
               new ConfirmCallback() {
                   @Override
                   public void confirm(ButtonInteraction interaction) throws Exception {
                       List<FileUpload> files = message.getAttachments().stream()
                                       .map(attachment -> {
                                           try {
                                               return FileUpload.fromData(attachment.retrieveInputStream().get(), GameId.generateId().concat(".png"));
                                           } catch (InterruptedException | ExecutionException e) {
                                               e.printStackTrace();
                                           }

                                           return null;
                                       })
                               .filter(Objects::nonNull)
                               .toList();

                       staffChannel.sendMessageEmbeds(getEmbed()
                               .simpleAuthoredEmbed()
                               .setAuthor(getLanguageManager().get("context.message.report"), "https://fluffici.eu", ICON_ALERT.getUrl())
                               .setThumbnail(message.getAuthor().getAvatarUrl())
                               .setDescription(getLanguageManager().get("common.message.content", message.getContentRaw()))
                               .addField(getLanguageManager().get("common.report.by"), interaction.getUser().getAsMention(), false)
                               .addField(getLanguageManager().get("common.author"), message.getAuthor().getAsMention(), true)
                               .addField(getLanguageManager().get("common.warns"), (sanctions.isEmpty() ? getLanguageManager().get("common.no_warns") : String.valueOf(sanctions.size())), true)
                               .setTimestamp(Instant.now())
                               .build()
                       ).addEmbeds(message.getEmbeds()).addFiles(files).addActionRow(
                               Button.link(String.format("https://discord.com/channels/%s/%s/%s", message.getGuildId(), message.getChannelId(), message.getId()), "Link")
                       ).queue();

                       interaction.replyEmbeds(getEmbed()
                               .simpleAuthoredEmbed()
                               .setAuthor(getLanguageManager().get("context.message.reported"), "https://fluffici.eu", ICON_ALERT.getUrl())
                               .setDescription(getLanguageManager().get("common.message.reported.desc", message.getAuthor().getAsMention()))
                               .setTimestamp(Instant.now())
                               .build()
                       ).setEphemeral(true).queue();
                   }

                   @Override
                   public void cancel(ButtonInteraction interaction) throws Exception {
                       interaction.reply(getLanguageManager().get("context.report.cancelled")).setEphemeral(true);
                   }
               }
       );
    }
}
