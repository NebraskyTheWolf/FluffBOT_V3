package eu.fluffici.bot.components.commands.fun;

/*
---------------------------------------------------------------------------------
File Name : CommandKiss.java

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


import com.google.gson.JsonArray;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@CommandHandle
public class CommandKiss extends Command {

    private final Random random = new Random();
    private final int EASTER_EGG_PERCENTAGE = 621; // UwU~
    private final String EASTER_EGG_URL = "https://c.tenor.com/56HF179F0nsAAAAC/tenor.gif";

    private final JsonArray images = this.getImages("kiss.json");


    public CommandKiss() {
        super("kiss", "Are you boy kisser? :)", CommandCategory.FUN);
        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select the user for this interaction", false));
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        if (interaction.getOptions().isEmpty()) {
            boolean hasRelationship = this.getUserManager().fetchRelationship(interaction.getUser(), interaction.getUser());
            if (hasRelationship) {
                List<User> users = new ArrayList<>();
                this.getUserManager().fetchAllRelationshipMembers(interaction.getUser()).forEach(jsonElement -> users.add(interaction.getHook().getJDA().getUserById(jsonElement.getUserId().getId())));

                String mentions = users.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" "));

                if (images.size() <= 0) {
                    interaction.reply(this.getLanguageManager().get("command.fun.no_image_found"));
                } else {
                    int randomIndex = new Random().nextInt(images.size());
                    String imageUrl = images.get(randomIndex - 1).getAsString();

                    boolean easterEgg = false;
                    if (random.nextInt(1000) > EASTER_EGG_PERCENTAGE) {
                        imageUrl = EASTER_EGG_URL;
                        easterEgg = true;
                    }

                    EmbedBuilder embedBuilder =  this.getEmbed()
                            .image(String.format("%s políbil/a %s", interaction.getUser().getEffectiveName(), "jeho/její partner/ka"), "", imageUrl);

                    if (easterEgg)
                        embedBuilder.setDescription(this.getLanguageManager().get("common.meme_kisser.rolled"));
                    interaction.replyEmbeds(embedBuilder.build()).setContent(mentions).addActionRow(Button.link("Original Post", imageUrl)).queue();
                }
            } else {
                interaction.reply(this.getLanguageManager().get("common.interact.select_user")).setEphemeral(true).queue();
            }
        } else {
            User target = interaction.getOption("user").getAsUser();

            if (this.getUserManager().isBlocked(target, interaction.getUser())) {
                interaction.reply(this.getLanguageManager().get("command.fun.interaction_blocked")).setEphemeral(true).queue();
                return;
            }

            if (images.size() <= 0) {
                interaction.reply(this.getLanguageManager().get("command.fun.no_image_found")).queue();
            } else {
                int randomIndex = new Random().nextInt(images.size() - 1);
                String imageUrl = images.get(randomIndex).getAsString();
                boolean easterEgg = false;
                if (random.nextInt(1000) > EASTER_EGG_PERCENTAGE) {
                    imageUrl = EASTER_EGG_URL;
                    easterEgg = true;
                }

                EmbedBuilder embedBuilder =  this.getEmbed()
                        .image(String.format("%s políbil/a %s", interaction.getUser().getEffectiveName(), target.getEffectiveName()), "", imageUrl);

                if (easterEgg)
                    embedBuilder.setDescription(this.getLanguageManager().get("common.meme_kisser.rolled"));

                interaction.replyEmbeds(embedBuilder.build()).setContent(target.getAsMention()).addActionRow(Button.link(imageUrl, "Original Post")).queue();
            }
        }
    }
}
