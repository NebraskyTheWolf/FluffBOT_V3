package eu.fluffici.bot.components.commands.fun;

/*
---------------------------------------------------------------------------------
File Name : CommandFox.java

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


import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

@CommandHandle
public class CommandFox extends Command {
    public CommandFox() {
        super("fox", "Get some foxxo image owo", CommandCategory.FUN);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://randomfox.ca/floof/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject json = new JSONObject(response.body().string());
                String imageUrl = json.getString("image");

                interaction.replyEmbeds(
                        this.getEmbed().image(this.getLanguageManager().get("command.fox.title"), "", imageUrl)
                                .setColor(Color.ORANGE).build()
                ).queue();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        interaction.reply(this.getLanguageManager().get("command.internal_error")).setEphemeral(true).queue();
    }
}
