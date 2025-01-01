package pw.znopp.Events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pw.znopp.Main;
import pw.znopp.Utils.TextChannels;
import pw.znopp.Utils.VoiceChannels;

import java.util.Map;
import java.util.Objects;

public class SlashCommandInteraction extends ListenerAdapter {

    VoiceChannels voiceChannels = VoiceChannels.getInstance();
    TextChannels textChannels = TextChannels.getInstance();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String textChannel = textChannels.GetTextChannel(event.getJDA(), event.getChannel().asTextChannel());

        Category category = event.getGuildChannel().asTextChannel().getParentCategory();

        if (category == null) {
            event.reply("This command can only be used in a channel within a category!").setEphemeral(true).queue();
            return;
        }

        if (event.getName().equalsIgnoreCase("create")) {

            if (!event.getChannel().getId().equals(textChannel)) {
                event.reply("Commands are not enabled in this channel!").setEphemeral(true).queue();
                return;
            }

            String channelName = Objects.requireNonNull(event.getOption("channel_name")).getAsString();
            Integer maxUsers = event.getOption("max_users") != null ? Objects.requireNonNull(event.getOption("max_users")).getAsInt() : null;

            event.deferReply(true).queue();


            category.createVoiceChannel(channelName).queue(voice_channel -> {

                // Deny the @everyone role the CONNECT permission
                voice_channel.upsertPermissionOverride(Objects.requireNonNull(event.getGuild()).getPublicRole())
                        .deny(Permission.VOICE_CONNECT)
                        .queue();

                // Allow the user who sent the command the CONNECT permission
                voice_channel.upsertPermissionOverride(Objects.requireNonNull(event.getMember()))
                        .grant(Permission.VOICE_CONNECT)
                        .queue();

                if (maxUsers != null) {
                    voice_channel.getManager().setUserLimit(maxUsers).queue();
                }

                voiceChannels.AddVoiceChannel(voice_channel, event.getUser().getId());
                voiceChannels.saveVoiceChannels();
                Main.logger.info("Saved voiceChannels list");

                event.getHook().sendMessage("Voice chanel ``" + channelName + "`` created!").queue();

            });
        }

        if (event.getName().equalsIgnoreCase("commands")) {

            event.deferReply(true).queue();

            boolean allowCreation = Objects.requireNonNull(event.getOption("enable")).getAsBoolean();

            if (allowCreation) {
                textChannels.AddTextChannel(event.getChannel().asTextChannel());
                textChannels.saveTextChannels();
                Main.logger.info("Added text channel and saved textChannels list");

                event.getHook().sendMessage("Text channel ``" + event.getChannel().getName() + "`` now has commands enabled!").queue();
            } else {
                textChannels.RemoveTextChannel(event.getChannel().asTextChannel());
                textChannels.saveTextChannels();
                Main.logger.info("Removed text channel and saved textChannels list");

                event.getHook().sendMessage("Text channel ``" + event.getChannel().getName() + "`` now has commands disabled!").queue();
            }
        }

        if (event.getName().equalsIgnoreCase("set-permissions")) {
            String channelName = Objects.requireNonNull(event.getOption("channel_name")).getAsString();
            String allowedUserId = Objects.requireNonNull(event.getOption("user")).getAsUser().getId();
            boolean canJoin = Objects.requireNonNull(event.getOption("can_join")).getAsBoolean();
            String userId = event.getUser().getId();

            Map<VoiceChannel, String> channels = voiceChannels.GetVoiceChannels(event.getJDA());

            VoiceChannel voiceChannel = channels.keySet().stream()
                    .filter(vc -> vc.getName().equalsIgnoreCase(channelName) && channels.get(vc).equals(userId))
                    .findFirst()
                    .orElse(null);

            if (voiceChannel == null) {
                event.reply("You don't own a voice channel with this name!").setEphemeral(true).queue();
                return;
            }

            if (!event.getChannel().getId().equals(textChannel)) {
                event.reply("Commands are not enabled in this channel!").setEphemeral(true).queue();
                return;
            }

            Guild guild = event.getGuild();

            if (canJoin) {
                guild.retrieveMemberById(allowedUserId).queue(member -> {
                    voiceChannel.upsertPermissionOverride(member)
                            .grant(Permission.VOICE_CONNECT)
                            .queue(success -> event.reply("User <@" + allowedUserId + "> can now access channel `" + channelName + "`!").queue(),
                                    error -> event.reply("Failed to update permissions for the user!").setEphemeral(true).queue());
                }, failure -> event.reply("Couldn't find the specified user!").setEphemeral(true).queue());
            } else {
                guild.retrieveMemberById(allowedUserId).queue(member -> {
                    voiceChannel.upsertPermissionOverride(member)
                            .deny(Permission.VOICE_CONNECT)
                            .queue(success -> event.reply("User <@" + allowedUserId + "> can no longer access channel `" + channelName + "`!").queue(),
                                    error -> event.reply("Failed to update permissions for the user!").setEphemeral(true).queue());
                }, failure -> event.reply("Couldn't find the specified user!").setEphemeral(true).queue());
            }
        }

        if (event.getName().equalsIgnoreCase("remove")) {

            String channelName = Objects.requireNonNull(event.getOption("channel_name")).getAsString();
            String userId = event.getUser().getId();

            Map<VoiceChannel, String> channels = voiceChannels.GetVoiceChannels(event.getJDA());

            VoiceChannel voiceChannel = channels.keySet().stream()
                    .filter(vc -> vc.getName().equalsIgnoreCase(channelName) && channels.get(vc).equals(userId))
                    .findFirst()
                    .orElse(null);

            if (voiceChannel == null) {
                event.reply("You don't own a voice channel with this name!").setEphemeral(true).queue();
                return;
            }

            if (!event.getChannel().getId().equals(textChannel)) {
                event.reply("Commands are not enabled in this channel!").setEphemeral(true).queue();
                return;
            }

            voiceChannel.delete().queue(
                    success -> {
                        event.reply("Voice channel ``" + channelName + "`` deleted!").setEphemeral(true).queue();
                        voiceChannels.RemoveVoiceChannel(voiceChannel);
                        voiceChannels.saveVoiceChannels();
                    },
                    failure -> event.reply("Failed to delete the voice channel!").setEphemeral(true).queue()
            );

        }

    }
}
