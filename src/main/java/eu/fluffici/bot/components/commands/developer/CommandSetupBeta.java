package eu.fluffici.bot.components.commands.developer;

/*
---------------------------------------------------------------------------------
File Name : CommandSetupBeta.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.concurrent.TimeUnit;
@CommandHandle
public class CommandSetupBeta extends Command {
    public CommandSetupBeta() {
        super("setup-beta", "Create the initial setup of the beta-testing server.", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

        this.getOptionData().add(new OptionData(OptionType.STRING, "message", "The target message id."));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        TextChannel channel = (TextChannel) interaction.getChannel();

        if (interaction.getOption("message") != null) {
            channel.editMessageEmbedsById(interaction.getOption("message").getAsString(), this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor("Program beta testování uzavřen", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1243933750748909661/clipboard-check.png?ex=66534715&is=6651f595&hm=8744463f35635dd4050978f6a138b6d094527b50157d34d53de06eef76cfde59&")
                    .setTitle("Děkujeme za účast!")
                    .setDescription(
                            """
                            Vážení beta testovatelé, \n
                            Chceme vyjádřit upřímné díky každému z vás za vaši účast v programu beta testování.
                            Vaše zpětná vazba a hlášení chyb byly nepostradatelné pro zlepšení Fluffici. Opravdu si vážíme vaší oddanosti a podpory.
                            Od dnešního dne je program beta testování uzavřen. Všechna data nasbíraná během tohoto období budeme přezkoumávat, abychom dále zlepšili uživatelskou zkušenost.
                            Ještě jednou děkujeme, že jste s námi byli součástí této cesty.
                            """
                    )
                    .setFooter("Pro jakékoliv další dotazy neváhejte nás kontaktovat. Přejeme vám pěkný den!", "https://cdn.discordapp.com/attachments/1224419443300372592/1225847106808447088/question-mark.png?ex=6652bcd5&is=66516b55&hm=1a3f07a894bbcbbba3f88960aa0678455a6b5dbcdb856b100efa8deb45daf809&")
                    .build()
            ).setActionRow(Button.success("beta:participate", "Připojit se k betě").asDisabled()).queue();

            Message tempMessage = channel.sendMessage(" @everyone ").complete();
            FluffBOT.getInstance().getExecutorMonoThread().schedule(() -> tempMessage.delete().reason("Dočasná zpráva s pingem").queue(), 5, TimeUnit.SECONDS);

            Role betaRole = interaction.getGuild().getRoleById("1243247800196665546");
            Role developerRole = interaction.getGuild().getRoleById("382918201241108481");
            if (betaRole != null && developerRole != null) {
                interaction.getGuild().getMembers()
                        .stream()
                        .filter(member -> member.getRoles().contains(betaRole))
                        .filter(member -> !member.getRoles().contains(developerRole))
                        .filter(member -> member.canInteract(interaction.getGuild().getSelfMember())).toList()
                        .forEach(member -> interaction.getGuild().removeRoleFromMember(member, betaRole).reason("Beta program uzavřen.").queue());
            }

            interaction.reply("Closing beta program.").setEphemeral(true).queue();
            return;
        }

        if (channel != null && channel.canTalk()) {
            channel.sendMessageEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor("Připojte se nyní k beta testovacímu programu!", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1225843626047836230/flask.png?ex=6652b997&is=66516817&hm=9969da238e848712f92ef87bdd30ce993370ff8713fed0afe1bb0f526b8faaa6&")
                    .setTitle("Požadavky na způsobilost pro beta testovací program")
                    .setDescription(
                            """
                            - Musíte být ověřeným členem na hlavním Discord serveru Fluffici.
                            - Na hlavním serveru nesmíte mít žádné nevyřízené sankce.
                            - Musíte přijmout, že **VŠECHNA DATA** budou na konci beta testu smazána.
                            - Musíte souhlasit, že všechny nalezené chyby oznámíte a nebudete je zneužívat.
                            """
                    )
                    .setFooter("Pokud souhlasíte s těmito podmínkami, klikněte na tlačítko níže, nebo opusťte server.", "https://cdn.discordapp.com/attachments/1224419443300372592/1225847106808447088/question-mark.png?ex=6652bcd5&is=66516b55&hm=1a3f07a894bbcbbba3f88960aa0678455a6b5dbcdb856b100efa8deb45daf809&")
                    .build()
            ).addActionRow(Button.success("beta:participate", "Připojte se k beta testování")).queue();
        }

        interaction.reply("Zpráva o zahájení beta testování odeslána!").setEphemeral(true).queue();
    }
}
