package eu.fluffici.furraid.components.commands;

/*
---------------------------------------------------------------------------------
File Name : CommandManager.java

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


import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.components.commands.admin.*;
import eu.fluffici.furraid.components.commands.misc.*;
import eu.fluffici.furraid.components.commands.moderator.CommandModeration;
import eu.fluffici.furraid.components.commands.moderator.CommandTicket;
import eu.fluffici.furraid.components.commands.moderator.CommandVerification;
import eu.fluffici.furraid.components.commands.staff.CommandGlobalBlacklist;
import eu.fluffici.furraid.components.commands.staff.CommandServerInfo;
import eu.fluffici.furraid.components.commands.staff.CommandUserFlags;
import eu.fluffici.furraid.components.commands.staff.CommandUserInfo;
import eu.fluffici.furraid.util.Embed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CommandManager {
    private final List<FCommand> commands = new ArrayList<>();
    private final Object[] lock = new Object[] {};

    private final FurRaidDB instance;

    public CommandManager(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * Loads the commands into the command manager.
     */
    public void load() {
        synchronized (this.lock) {
            this.commands.clear();

            // Administration
            this.commands.add(new CommandLocalBlacklist());
            this.commands.add(new CommandWhitelist());
            this.commands.add(new CommandSettings());

            // Staff
            this.commands.add(new CommandGlobalBlacklist());
            this.commands.add(new CommandUserInfo());
            this.commands.add(new CommandServerInfo());
            this.commands.add(new CommandUserFlags());

            // Moderator
            this.commands.add(new CommandVerification());
            this.commands.add(new CommandModeration());
            this.commands.add(new CommandTicket(this.instance));

            // Misc
            this.commands.add(new CommandHelp());
            this.commands.add(new CommandAbout());
            this.commands.add(new CommandDonate());
            this.commands.add(new CommandSupport());
            this.commands.add(new CommandFeedback());

            this.commands.forEach(cmd -> {
                cmd.setEmbed(new Embed(this.instance));
                cmd.setLanguageManager(FurRaidDB.getInstance().getLanguageManager());
                this.instance.getJda().addEventListener(cmd);
            });
        }
    }

    public List<FCommand> getAllCommands() {
        return this.commands;
    }

    public FCommand findByName(String name) {
        AtomicReference<FCommand> commandReference = new AtomicReference<>();

        this.commands.forEach(cmd -> {
            if (cmd.getName().equals(name)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }
}
