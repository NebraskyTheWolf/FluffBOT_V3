package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandBirthday.java

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
import eu.fluffici.bot.api.beans.players.BirthdayBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.*;
import java.time.format.DateTimeFormatter;

@CommandHandle
@SuppressWarnings("All")
public class CommandBirthday extends Command {

    private final FluffBOT instance;

    public CommandBirthday(FluffBOT instance) {
        super("birthday", "This command allows you to add your birthdate to your user profile", CommandCategory.MISC);

        this.instance = instance;

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getOptionData().add(new OptionData(OptionType.INTEGER, "day", "The day of your birth (1 to 31)", true));
        this.getOptionData().add(new OptionData(OptionType.INTEGER, "month", "The month of your birth (1 to 12)", true));
        this.getOptionData().add(new OptionData(OptionType.INTEGER, "year", "The year of your birth (4 digits)", true));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        int currentYear = Year.now().getValue();

        int day = interaction.getOption("day").getAsInt();
        int month = interaction.getOption("month").getAsInt();
        int year = interaction.getOption("year").getAsInt();

        if (Math.abs((year - currentYear)) < 15) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.birthdate.too_young")
            )).queue();
            interaction.getGuild()
                    .getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.logging"))
                    .sendMessage(String.format("The user %s tried to register a birthdate under 15 years old, Birthdate: (%s-%s-%s)", "<@" + interaction.getUser().getId() + ">", day, month, year))
                    .queue();
        } else if (year < (currentYear - 130) || year >= currentYear || day <= 0 || day > 31 || month <= 0 || month > 12) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.birthdate.invalid")
            )).queue();
        } else {
            this.getUserManager().createBirthdate(new BirthdayBean(
                    interaction.getUser().getId(),
                    month,
                    day,
                    year,
                    null,
                    null
            ));

            LocalDate birthdate = LocalDate.of(year, Month.of(month), day);
            LocalDate today = LocalDate.now();
            LocalDate nextBirthdate = birthdate.withYear(today.getYear());
            if (nextBirthdate.isBefore(today) || nextBirthdate.isEqual(today)) {
                nextBirthdate = nextBirthdate.plusYears(1);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd, MMMM, yyyy");

            interaction.replyEmbeds(this.buildSuccess(
                    this.getLanguageManager().get("command.birthdate.success",  nextBirthdate.format(formatter)))
            ).queue();
        }
    }
}
