package eu.fluffici.bot.components.scheduler.user;

/*
---------------------------------------------------------------------------------
File Name : BirthdateScheduler.java

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
import eu.fluffici.bot.api.beans.players.BirthdayBean;
import eu.fluffici.bot.api.interactions.Task;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
public class BirthdateScheduler extends Task {

    private final List<String> birthdays = new CopyOnWriteArrayList<>();

    private final FluffBOT instance;

    public BirthdateScheduler(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading BirthdateScheduler scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                List<BirthdayBean> birthdayBeanList = this.instance.getGameServiceManager().getAllBirthdate();
                for (BirthdayBean birthdayBean : birthdayBeanList) {
                    User user = this.instance.getJda().getUserById(birthdayBean.getUserId());
                    if (user != null) {
                        LocalDate currentDate = LocalDate.now();
                        LocalDate birthdate = LocalDate.of(currentDate.getYear(), birthdayBean.getMonth(), birthdayBean.getDay());

                        LocalDate lastNotification = null;
                        if (birthdayBean.getLastNotification() != null)
                            lastNotification = LocalDate.of(currentDate.getYear(), birthdayBean.getLastNotification().getMonth(), birthdayBean.getLastNotification().getDay());
                        if (lastNotification != null && !lastNotification.equals(currentDate))
                            continue;

                        if (birthdate.equals(currentDate)) {
                            if (!this.birthdays.contains(birthdayBean.getUserId())) {
                                boolean result = this.handleBirthday(birthdayBean);
                                if (result) {
                                    this.birthdays.add(birthdayBean.getUserId());
                                } else {
                                    this.instance.getLogger().debug("A error occurred while synchronising %s birthdate.", birthdayBean.getUserId());
                                }
                            }
                        } else {
                            if (this.birthdays.contains(birthdayBean.getUserId())) {
                                boolean result = this.handleNonBirthday(birthdayBean);
                                if (result) {
                                    this.birthdays.remove(birthdayBean.getUserId());
                                } else {
                                    this.instance.getLogger().debug("A error occurred while synchronising %s birthdate.", birthdayBean.getUserId());
                                }
                            }

                            //this.instance.getLogger().debug("This is not the birthday of %s", birthdayBean.getUserId());
                        }
                    }
                }
            } catch (Exception e) {
                this.instance.getLogger().error("Failed to synchronise birthdays. ", e);
                e.printStackTrace();
            }
            // this.instance.getLogger().debug("Job 'birthdays_check' finished.");
        }, 60, 60, TimeUnit.SECONDS);
    }

    private boolean handleBirthday(BirthdayBean birthdayBean) {
        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));

        if (guild != null && birthdayBean != null) {
            Member self = guild.getSelfMember();
            Member user = guild.getMemberById(birthdayBean.getUserId());
            Role birthdayRole = guild.getRoleById(this.instance.getDefaultConfig().getProperty("role.birthday"));

            if (birthdayRole != null) {
                if (user != null) {
                    if (self.canInteract(user)) {
                        guild.addRoleToMember(user, birthdayRole).reason("Birthday").queue();

                        if (birthdayBean.getLastRewardAt() == null) {
                            try {
                                this.instance.getUserManager().addTokens(this.instance.getUserManager().fetchUser(user), 10);
                                birthdayBean.setLastRewardAt(new Timestamp(Instant.now().toEpochMilli()));

                                this.instance.getGameServiceManager().updateBirthdate(birthdayBean);
                            } catch (Exception e) {
                                this.instance.getLogger().error("A error occurred while adding flufftokens for birthday (" + birthdayBean.getUserId() + ")", e);
                            }

                            this.instance.getLogger().debug("%s got %s flufftokens, %s", birthdayBean.getUserId(), 10, birthdayBean.getLastRewardAt());
                        } else {
                            LocalDateTime localDateTime = birthdayBean.getLastRewardAt().toLocalDateTime();
                            if (localDateTime.getYear() >= 1) {
                                this.instance.getUserManager().addTokens(this.instance.getUserManager().fetchUser(user), 10);
                                this.instance.getLogger().debug("%s got %s flufftokens (null-check)", birthdayBean.getUserId(), 10);
                            } else {
                                this.instance.getLogger().debug("%s cannot get flufftokens, ( 1 Year delay )", birthdayBean.getUserId(), 10);
                            }
                        }

                        TextChannel channel = guild.getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.birthday"));
                        if (birthdayBean.getLastNotification() == null) {
                            try {
                                birthdayBean.setLastNotification(new Timestamp(Instant.now().toEpochMilli()));
                                if (channel != null && self.hasAccess(channel)) {
                                    channel.sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed(
                                                            user.getUser(),
                                                            this.instance.getLanguageManager().get("scheduler.birthday.title", user.getUser().getEffectiveName()),
                                                            this.instance.getLanguageManager().get("scheduler.birthday.desc", Math.abs(birthdayBean.getYear() - LocalDate.now().getYear())),
                                                            Color.CYAN
                                                    )
                                                    .build()
                                    ).mention(user).queue();
                                } else {
                                    this.instance.getLogger().warn("The birthday channel does not exist or I don't have the permission to write here.");
                                }
                                this.instance.getGameServiceManager().updateBirthdate(birthdayBean);
                            } catch (Exception e) {
                                this.instance.getLogger().error("A error occurred while adding flufftokens for birthday (" + birthdayBean.getUserId() + ")", e);
                            }
                        } else {
                            LocalDateTime localDateTime = birthdayBean.getLastNotification().toLocalDateTime();
                            if (localDateTime.getYear() >= 1) {
                                if (channel != null && self.hasAccess(channel)) {
                                    channel.sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed(
                                                            user.getUser(),
                                                            this.instance.getLanguageManager().get("scheduler.birthday.title", user.getUser().getEffectiveName()),
                                                            this.instance.getLanguageManager().get("scheduler.birthday.desc", Math.abs(birthdayBean.getYear() - LocalDate.now().getYear())),
                                                            Color.CYAN
                                                    )
                                                    .build()
                                    ).mention(user).queue();
                                } else {
                                    this.instance.getLogger().warn("The birthday channel does not exist or I don't have the permission to write here.");
                                }
                            }
                        }
                        return true;
                    } else {
                        this.instance.getLogger().warn("Cannot interact with %s because the permissions is insufficient.", user.getUser().getName());
                    }
                } else {
                    this.instance.getLogger().warn("%s left the server and cannot be updated.", birthdayBean.getUserId());
                }
            } else {
                this.instance.getLogger().warn("The birthday role is missing.");
            }
        } else {
            this.instance.getLogger().warn("The main guild does not exist.");
        }
        return false;
    }

    private boolean handleNonBirthday(BirthdayBean birthdayBean) {
        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));

        if (guild != null) {
            Member user = guild.getMemberById(birthdayBean.getUserId());
            Member self = guild.getSelfMember();

            Role birthdayRole = guild.getRoleById(this.instance.getDefaultConfig().getProperty("role.birthday"));

            if (birthdayRole != null) {
                if (user != null) {
                    if (self.canInteract(user)) {
                        guild.removeRoleFromMember(user, birthdayRole).reason("Birthday passed.").queue();
                        return true;
                    } else {
                        this.instance.getLogger().warn("Cannot interact with %s because the permissions is insufficient.", user.getUser().getName());
                    }
                } else {
                    this.instance.getLogger().warn("%s left the server and cannot be updated.", birthdayBean.getUserId());
                }
            } else {
                this.instance.getLogger().warn("The birthday role is missing.");
            }
        } else {
            this.instance.getLogger().warn("The main guild does not exist.");
        }

        return false;
    }
}
