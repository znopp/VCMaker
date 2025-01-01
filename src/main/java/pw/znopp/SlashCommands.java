package pw.znopp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SlashCommands extends ListenerAdapter {

    public SlashCommands(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            guild.updateCommands().addCommands(
                    Commands.slash("create", "Creates a voice channel")
                            .addOption(OptionType.STRING, "channel_name", "The name of the channel", true)
                            .addOption(OptionType.INTEGER, "max_users", "How many people can join the channel", false),

                    Commands.slash("commands", "Enable or disable commands in this channel")
                            .addOption(OptionType.BOOLEAN, "enable", "Toggles ability to use commands in this channel", true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

                    Commands.slash("set-permissions", "Sets user permissions for a voice channel")
                            .addOption(OptionType.STRING, "channel_name", "The name of the channel to set permissions for", true)
                            .addOption(OptionType.MENTIONABLE, "user", "User or role to allow access", true)
                            .addOption(OptionType.BOOLEAN, "can_join", "Whether they can join or not", true),

                    Commands.slash("remove", "Removes a voice channel")
                            .addOption(OptionType.STRING, "channel_name", "The name of the channel to remove", true)

            ).queue(success -> Main.logger.info("Added slash commands to guild {}", guild.getName()));
        }
    }

}
