package pw.znopp.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import pw.znopp.Main;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class VoiceChannels {

    private static VoiceChannels instance;
    private final Map<String, String> voiceChannelMap; // Stores channel ID as key, user ID as value
    private static final String SAVE_FILE_PATH = "data/voiceChannels.json";
    private final Gson gson;

    private void ensureDataFolderExists() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private VoiceChannels() {
        ensureDataFolderExists();
        voiceChannelMap = new HashMap<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static VoiceChannels getInstance() {
        if (instance == null) {
            instance = new VoiceChannels();
        }
        return instance;
    }

    // Add a voice channel with the associated user ID
    public void AddVoiceChannel(VoiceChannel channel, String userId) {
        if (channel != null && !voiceChannelMap.containsKey(channel.getId())) {
            voiceChannelMap.put(channel.getId(), userId);
        }
    }

    // Remove a voice channel
    public void RemoveVoiceChannel(VoiceChannel channel) {
        voiceChannelMap.remove(channel.getId());
    }

    // Get a list of all voice channels
    public Map<VoiceChannel, String> GetVoiceChannels(JDA jda) {
        Map<VoiceChannel, String> channels = new HashMap<>();
        for (Map.Entry<String, String> entry : voiceChannelMap.entrySet()) {
            VoiceChannel channel = jda.getVoiceChannelById(entry.getKey());
            if (channel != null) {
                channels.put(channel, entry.getValue());
            }
        }
        return channels;
    }

    // Check if a channel exists and return the associated user ID
    public String GetVoiceChannel(VoiceChannel voiceChannel) {
        if (voiceChannel != null && voiceChannelMap.containsKey(voiceChannel.getId())) {
            return voiceChannel.getId(); // Return the channel ID
        }
        return null; // Return null if the channel doesn't exist
    }


    // Save the voice channel map to a JSON file
    public void saveVoiceChannels() {
        try (Writer writer = new FileWriter(SAVE_FILE_PATH)) {
            gson.toJson(voiceChannelMap, writer);
        } catch (IOException e) {
            Main.logger.error(e.getMessage());
        }
    }

    // Load the voice channel map from a JSON file
    public void loadVoiceChannels() {
        File file = new File(SAVE_FILE_PATH);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type mapType = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> loadedChannelMap = gson.fromJson(reader, mapType);
                voiceChannelMap.clear();
                voiceChannelMap.putAll(loadedChannelMap);
                Main.logger.info("Voice channels loaded!");
            } catch (IOException e) {
                Main.logger.error(e.getMessage());
            }
        }
    }
}
