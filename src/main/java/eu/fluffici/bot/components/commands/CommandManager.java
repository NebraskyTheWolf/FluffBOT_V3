package eu.fluffici.bot.components.commands;

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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.components.commands.admin.*;
import eu.fluffici.bot.components.commands.developer.*;
import eu.fluffici.bot.components.commands.developer.CommandMigrate;
import eu.fluffici.bot.components.commands.economy.CommandDonate;
import eu.fluffici.bot.components.commands.economy.CommandShop;
import eu.fluffici.bot.components.commands.economy.CommandUpvote;
import eu.fluffici.bot.components.commands.fun.*;
import eu.fluffici.bot.components.commands.games.*;
import eu.fluffici.bot.components.commands.misc.*;
import eu.fluffici.bot.components.commands.moderator.*;
import eu.fluffici.bot.components.commands.profile.*;
import eu.fluffici.bot.utils.Embed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CommandManager {
    private final List<Command> commands = new ArrayList<>();
    private final Object[] lock = new Object[] {};

    private final FluffBOT instance;

    public CommandManager(FluffBOT instance) {
        this.instance = instance;
    }

    /**
     * Loads the commands into the command manager.
     */
    public void load() {
        synchronized (this.lock) {
            this.commands.clear();

            // Prioritised Profile
            this.commands.add(new CommandInventory());

            // Admin
            this.commands.add(new CommandToken());
            this.commands.add(new CommandCoins());
            this.commands.add(new CommandPoints());
            this.commands.add(new CommandArtist());
            this.commands.add(new CommandGive());
            this.commands.add(new CommandClearRoles());
            this.commands.add(new CommandEconomicStatistic());
            this.commands.add(new CommandRestrictAccess());
            this.commands.add(new CommandPurge());
            this.commands.add(new CommandAutoReaction());
            this.commands.add(new CommandLevelRole());
            this.commands.add(new CommandNickset());

            // Economy
            this.commands.add(new CommandDonate());
            this.commands.add(new CommandUpvote());
            this.commands.add(new CommandShop());

            // Fun
            this.commands.add(new CommandFox());
            this.commands.add(new CommandCookie());
            this.commands.add(new CommandBoop());
            this.commands.add(new CommandHug());
            this.commands.add(new CommandKiss());
            this.commands.add(new CommandCuddles());
            this.commands.add(new CommandPats());

            // Games
            this.commands.add(new CommandCasino());
            this.commands.add(new CommandFish());
            this.commands.add(new CommandCraft());
            this.commands.add(new CommandGamble());
            this.commands.add(new CommandPracuj());
            this.commands.add(new CommandFarm());

            // Misc
            this.commands.add(new CommandBirthday(this.instance));
            this.commands.add(new CommandNickname(this.instance));
            this.commands.add(new CommandRelationship(this.instance));
            this.commands.add(new CommandClan(this.instance));
            this.commands.add(new CommandLeaderboard());
            this.commands.add(new CommandChannel());
            this.commands.add(new CommandSubscribe());
            this.commands.add(new CommandAbout());
            this.commands.add(new CommandHelp());
            this.commands.add(new CommandFursona());

            // Moderation
            this.commands.add(new CommandMute(this.instance));
            this.commands.add(new CommandBan(this.instance));
            this.commands.add(new CommandWarn(this.instance));
            this.commands.add(new CommandUnban(this.instance));
            this.commands.add(new CommandHistory(this.instance));
            this.commands.add(new CommandStatistics(this.instance));
            this.commands.add(new CommandTicket(this.instance));
            this.commands.add(new CommandVerification());

            // Profile
            this.commands.add(new CommandAchievement(this.instance));
            this.commands.add(new CommandProfile());
            this.commands.add(new CommandBlock());
            this.commands.add(new CommandRoles());
            this.commands.add(new CommandProfileMigrate());
            this.commands.add(new CommandData());

            // Developer
            this.commands.add(new CommandSpawn());
            this.commands.add(new CommandSetupBeta());
            this.commands.add(new CommandAddAllBeta());
            this.commands.add(new CommandShutdown());
            this.commands.add(new CommandRefreshMap());
            this.commands.add(new CommandMigrate());
            this.commands.add(new CommandConvert());
            this.commands.add(new CommandSetupVerification());
            this.commands.add(new CommandSetupTicket());
            this.commands.add(new CommandTestPodium());

            this.commands.forEach(cmd -> {
                cmd.setEmbed(new Embed(this.instance));
                cmd.setLanguageManager(FluffBOT.getInstance().getLanguageManager());
                cmd.setUserManager(FluffBOT.getInstance().getUserManager());
                cmd.setFoodManager(FluffBOT.getInstance().getFoodItemManager());
                this.instance.getJda().addEventListener(cmd);
            });
        }
    }

    public List<Command> getAllCommands() {
        return this.commands;
    }

    public Command findByName(String name) {
        AtomicReference<Command> commandReference = new AtomicReference<>();

        this.commands.forEach(cmd -> {
            if (cmd.getName().equals(name)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }
}
