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

import java.io.*;
import java.util.EnumSet;
import java.util.Properties;

public class Main extends ListenerAdapter {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String CONFIG_FILE = "config.properties";
    private static Properties config;

    public static Properties getConfig() {
        return config;
    }

    public static void main(String[] args) {
        config = loadConfig();
        String token = config.getProperty("token");

        if (token == null || token.isEmpty()) {
            logger.error("Bot token not found in config.properties! Please set your token in the config file.");
            createDefaultConfig();
            System.exit(1);
        }

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.SCHEDULED_EVENTS
        );

        JDABuilder.createDefault(token, intents)
                .addEventListeners(new Main())
                .addEventListeners(new SlashCommandInteraction())
                .addEventListeners(new GuildVoiceUpdate())
                .build();
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            logger.info("Config file not found. Creating default config file...");
            createDefaultConfig();
            logger.info("Please set your bot token in " + CONFIG_FILE);
            System.exit(1);
        }

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading config.properties file: {}", e.getMessage());
            System.exit(1);
        }
        return properties;
    }

    private static void createDefaultConfig() {
        Properties properties = new Properties();

        // If running in console, we can prompt for the token
        if (System.console() != null) {
            logger.info("No config file found. Let's create one!");
            String token = System.console().readLine("Please enter your bot token: ");
            properties.setProperty("token", token);
        } else {
            properties.setProperty("token", "your-token-here");
        }

        properties.setProperty("maxChannelsPerUser", "4");
        properties.setProperty("timeoutDuration", "10");

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Discord Bot Configuration");
            logger.info("Config file created at: " + new File(CONFIG_FILE).getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error creating config file: {}", e.getMessage());
        }
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