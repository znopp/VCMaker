package pw.znopp.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pw.znopp.Main;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TextChannels {

    private static TextChannels instance;
    private final ArrayList<String> textChannelIds;
    private static final String SAVE_FILE_PATH = "data/textChannels.json";
    private final Gson gson;

    private void ensureDataFolderExists() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private TextChannels() {
        ensureDataFolderExists();
        textChannelIds = new ArrayList<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static TextChannels getInstance() {
        if (instance == null) {
            instance = new TextChannels();
        }
        return instance;
    }

    public void AddTextChannel(TextChannel channel) {
        if (channel != null && !textChannelIds.contains(channel.getId())) {
            textChannelIds.add(channel.getId());
        }
    }

    public void RemoveTextChannel(TextChannel channel) {
        textChannelIds.remove(channel.getId());
    }

    public ArrayList<TextChannel> GetTextChannels(JDA jda) {
        ArrayList<TextChannel> channels = new ArrayList<>();
        for (String id : textChannelIds) {
            TextChannel channel = jda.getTextChannelById(id);
            if (channel != null) {
                channels.add(channel);
            }
        }
        return channels;
    }

    public String GetTextChannel(JDA jda, TextChannel textChannel) {
        return textChannelIds.stream()
                .filter(chan -> chan.equals(textChannel.getId()))
                .findFirst()
                .orElse(null);
    }

    // Save the voice channels to a JSON file
    public void saveTextChannels() {
        try (Writer writer = new FileWriter(SAVE_FILE_PATH)) {
            gson.toJson(textChannelIds, writer);
        } catch (IOException e) {
            Main.logger.error(e.getMessage());
        }
    }

    // Load the voice channels from a JSON file
    public void loadTextChannels() {
        File file = new File(SAVE_FILE_PATH);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> loadedChannelIds = gson.fromJson(reader, listType);
                textChannelIds.clear();
                textChannelIds.addAll(loadedChannelIds);
                Main.logger.info("Text channels loaded!");
            } catch (IOException e) {
                Main.logger.error(e.getMessage());
            }
        }
    }
}
