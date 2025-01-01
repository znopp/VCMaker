package pw.znopp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.znopp.Events.SlashCommandInteraction;
import pw.znopp.Events.GuildVoiceUpdate;
import pw.znopp.Utils.TextChannels;
import pw.znopp.Utils.VoiceChannels;

import java.util.EnumSet;


public class Main extends ListenerAdapter {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.SCHEDULED_EVENTS
        );

        JDABuilder.createDefault(args[0], intents)
                .addEventListeners(new Main())
                .addEventListeners(new SlashCommandInteraction())
                .addEventListeners(new GuildVoiceUpdate())
                .build();
    }

    @Override
    public void onReady(ReadyEvent event) {

        Main.logger.info("Ready!");

        JDA api = event.getJDA();

        VoiceChannels.getInstance().loadVoiceChannels();
        TextChannels.getInstance().loadTextChannels();

        new SlashCommands(api);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        VoiceChannels.getInstance().saveVoiceChannels();
        TextChannels.getInstance().saveTextChannels();
        Main.logger.info("Voice and text channels saved on shutdown. Goodnight!");
    }
}