package pw.znopp.Events;

import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pw.znopp.Main;
import pw.znopp.Utils.VoiceChannels;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuildVoiceUpdate extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    VoiceChannels voiceChannels = VoiceChannels.getInstance();

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelLeft = event.getChannelLeft();
        int timeoutDuration = Integer.parseInt(Main.getConfig().getProperty("timeoutDuration", "10"));

        if (channelLeft != null) {
            String chan = voiceChannels.GetVoiceChannel(channelLeft.asVoiceChannel());

            if (chan == null) return;

            if (!channelLeft.getId().equals(chan)) return;

            if (!channelLeft.getMembers().isEmpty()) return;

            scheduler.schedule(() -> {
                if (channelLeft.getMembers().isEmpty()) {
                    channelLeft.delete().queue();
                    voiceChannels.RemoveVoiceChannel(channelLeft.asVoiceChannel());
                    Main.logger.info("Removed channel {}", channelLeft.asVoiceChannel().getId());
                    VoiceChannels.getInstance().saveVoiceChannels();
                    Main.logger.info("Saved voiceChannels list");
                }
            }, timeoutDuration, TimeUnit.SECONDS);
        }

        if (event.getChannelJoined() != null && event.getChannelJoined().equals(channelLeft)) {
            scheduler.shutdownNow();
        }

    }
}
