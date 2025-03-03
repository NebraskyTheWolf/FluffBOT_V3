package eu.fluffici.bot.components.button.beta;

/*
---------------------------------------------------------------------------------
File Name : JoinBetaButton.java

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


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Objects;

public class JoinBetaButton extends ButtonBuilder {
    public JoinBetaButton() {
        super("beta:participate", "Join Beta!", ButtonStyle.SECONDARY);
    }

    @Override
    public void execute(ButtonInteraction interaction) {
        if (interaction.getMember().getRoles().contains(interaction.getGuild().getRoleById("1243247800196665546"))) {
            interaction.reply("Omlouváme se, ale již jste zapsáni do beta testovacího programu.").setEphemeral(true).queue();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://frdbapi.fluffici.eu/api/users/" + interaction.getUser().getId() + "/is-verified")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                if (data.has("data")) {
                    boolean isVerified = data.get("data").getAsJsonObject().get("verified").getAsBoolean();

                    if (isVerified) {
                        handleVerified(interaction);
                    } else {
                        interaction.reply("Omlouváme se, ale nejste způsobilí. Nejprve ověřte svůj účet na Discord serveru Fluffici.").setEphemeral(true).queue();
                    }
                } else {
                    interaction.reply("Omlouváme se, ale nemůžeme ověřit stav vašeho účtu. Zkuste to prosím znovu.").setEphemeral(true).queue();
                }
            } else {
                interaction.reply("Omlouváme se, ale nemůžeme ověřit stav vašeho účtu. Zkuste to prosím znovu.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            FluffBOT.getInstance().getLogger().error("Při žádosti o ověření stavu účtu pro " + interaction.getUser().getId() + " došlo k chybě.", e);
            interaction.reply("Omlouváme se, ale nemůžeme ověřit stav vašeho účtu. Zkuste to prosím znovu.").setEphemeral(true).queue();
        }
    }

    private void handleVerified(ButtonInteraction interaction) {
        FluffBOT.getInstance().getJda().getGuildById(interaction.getGuild().getId()).addRoleToMember(
                Objects.requireNonNull(interaction.getMember()),
                Objects.requireNonNull(FluffBOT.getInstance().getJda().getRoleById("1243247800196665546"))
        ).reason("Způsobilý pro 'program beta testování'.").queue();

        FluffBOT.getInstance().getAchievementManager().unlock(interaction.getMember(), 35);

        interaction.reply("Děkuji za vaši podporu! Nyní máte přístup k Discord serveru.").setEphemeral(true).queue();
    }
}
