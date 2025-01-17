package pw.znopp.Events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pw.znopp.Main;
import pw.znopp.Utils.TextChannels;
import pw.znopp.Utils.VoiceChannels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

            // Check how many channels the user already has
            String userId = event.getUser().getId();
            int maxChannelsPerUser = Integer.parseInt(Main.getConfig().getProperty("maxChannelsPerUser", "4"));

            long userChannelCount = voiceChannels.GetVoiceChannels(event.getJDA())
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().getGuild().equals(event.getGuild()))
                    .filter(entry -> entry.getValue().equals(userId))
                    .count();

            if (userChannelCount >= maxChannelsPerUser) {
                event.reply("You already have " + maxChannelsPerUser + " channels! Please delete one before creating a new one.").setEphemeral(true).queue();
                return;
            }

            Integer maxUsers = event.getOption("max_users") != null ? Objects.requireNonNull(event.getOption("max_users")).getAsInt() : null;
            long delay = event.getOption("delay") != null ? (long) Math.ceil(Objects.requireNonNull(event.getOption("delay")).getAsDouble() * 60) : 0;

            String originalChannelName = Objects.requireNonNull(event.getOption("channel_name")).getAsString();
            String userName = event.getMember().getUser().getName();

            // Calculate maximum length available for the channel name
            int maxChannelNameLength = 100; // Discord's limit
            int separatorLength = 1; // Length of the "-" character
            int counterMaxLength = 1; // Assume max 9 duplicates
            int maxOriginalNameLength = maxChannelNameLength - userName.length() - separatorLength - counterMaxLength;

            // Truncate original channel name if it's too long
            if (originalChannelName.length() > maxOriginalNameLength) {
                originalChannelName = originalChannelName.substring(0, maxOriginalNameLength);
            }

            String baseChannelName = userName + "-" + originalChannelName;
            String finalChannelName = baseChannelName;

            int counter = 1;
            while (!event.getGuild().getVoiceChannelsByName(finalChannelName, true).isEmpty()) {
                counter++;
                finalChannelName = baseChannelName + "-" + counter;

                // Check if adding counter would exceed max length
                if (finalChannelName.length() > maxChannelNameLength) {
                    // Truncate base name to accommodate counter
                    int requiredSpace = String.valueOf(counter).length() + 1; // +1 for the separator
                    baseChannelName = baseChannelName.substring(0, maxChannelNameLength - requiredSpace);
                    finalChannelName = baseChannelName + "-" + counter;
                }
            }

            String determinedChannelName = finalChannelName;

            event.deferReply(true).queue();

            String timeMessage = getTimeMessage(delay);

            event.getHook().sendMessage(
                    "Channel ``" + finalChannelName + "`` will be created in " + timeMessage + "!"
            ).setEphemeral(true).queue();

            category.createVoiceChannel(finalChannelName).queueAfter(delay, TimeUnit.SECONDS, voice_channel -> {
                // Rest of the channel creation code remains the same...
                voice_channel.upsertPermissionOverride(Objects.requireNonNull(event.getGuild()).getPublicRole())
                        .deny(Permission.VOICE_CONNECT)
                        .queue();

                voice_channel.upsertPermissionOverride(Objects.requireNonNull(event.getMember()))
                        .grant(Permission.VOICE_CONNECT)
                        .queue();

                if (maxUsers != null) {
                    voice_channel.getManager().setUserLimit(maxUsers).queue();
                }

                voiceChannels.AddVoiceChannel(voice_channel, event.getUser().getId());
                voiceChannels.saveVoiceChannels();
                Main.logger.info("Saved voiceChannels list");

                event.getHook().sendMessage("Voice channel ``" + determinedChannelName + "`` created! <@" + event.getUser().getId() + ">").setEphemeral(true).queue();
            });
        }


        if (event.getName().equalsIgnoreCase("commands")) {

            event.deferReply(true).queue();

            boolean allowCreation = Objects.requireNonNull(event.getOption("enable")).getAsBoolean();

            if (allowCreation) {
                textChannels.AddTextChannel(event.getChannel().asTextChannel());
                textChannels.saveTextChannels();
                Main.logger.info("Added text channel and saved textChannels list");

                event.getHook().sendMessage("Text channel ``" + event.getChannel().getName() + "`` now has commands enabled!").setEphemeral(true).queue();
            } else {
                textChannels.RemoveTextChannel(event.getChannel().asTextChannel());
                textChannels.saveTextChannels();
                Main.logger.info("Removed text channel and saved textChannels list");

                event.getHook().sendMessage("Text channel ``" + event.getChannel().getName() + "`` now has commands disabled!").setEphemeral(true).queue();
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

            if (guild == null) {
                event.reply("The guild is null! Aborting...").setEphemeral(true).queue();
                return;
            }

            if (canJoin) {
                guild.retrieveMemberById(allowedUserId).queue(member -> voiceChannel.upsertPermissionOverride(member)
                        .grant(Permission.VOICE_CONNECT)
                        .queue(
                            success -> event.reply("User <@" + allowedUserId + "> can now access channel `" + channelName + "`!").queue(),
                            error -> event.reply("Failed to update permissions for the user!").setEphemeral(true).queue()),
                            failure -> event.reply("Couldn't find the specified user!").setEphemeral(true).queue());
            } else {
                guild.retrieveMemberById(allowedUserId).queue(member -> voiceChannel.upsertPermissionOverride(member)
                        .deny(Permission.VOICE_CONNECT)
                        .queue(
                            success -> event.reply("User <@" + allowedUserId + "> can no longer access channel `" + channelName + "`!").queue(),
                            error -> event.reply("Failed to update permissions for the user!").setEphemeral(true).queue()),
                            failure -> event.reply("Couldn't find the specified user!").setEphemeral(true).queue());
            }
        }

        if (event.getName().equalsIgnoreCase("remove")) {

            String channelName = Objects.requireNonNull(event.getOption("channel_name")).getAsString();
            String userId = event.getUser().getId();

            Map<VoiceChannel, String> channels = voiceChannels.GetVoiceChannels(event.getJDA())
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().getGuild().equals(event.getGuild())) // Same guild only
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            boolean isAdmin = event.getGuild().getMemberById(event.getUser().getId()).hasPermission(Permission.ADMINISTRATOR);

            VoiceChannel voiceChannel = channels.keySet().stream()
                    .filter(vc ->
                        vc.getName().contains(channelName) && // Match the name
                        (isAdmin || channels.get(vc).equals(userId)) // Admins can delete any, non-admins only their own
                    )
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

    @NotNull
    public static String getTimeMessage(long seconds) {
        if (seconds == 0) return "0 seconds";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        List<String> parts = new ArrayList<>();

        if (hours > 0) {
            parts.add(hours + " hour" + (hours > 1 ? "s" : ""));
        }
        if (minutes > 0) {
            parts.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
        }
        if (remainingSeconds > 0) {
            parts.add(remainingSeconds + " second" + (remainingSeconds > 1 ? "s" : ""));
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }
        if (parts.size() == 2) {
            return parts.get(0) + " and " + parts.get(1);
        }
        return parts.get(0) + ", " + parts.get(1) + " and " + parts.get(2);
    }
}
