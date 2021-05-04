package me.nieuwdorp;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class VoiceChannelListener extends ListenerAdapter {

    private AudioPlayerManager playerManager = null;
    private AudioPlayer player = null;
    private AudioPlayerSendHandler audioPlayerSendHandler = null;
    private TrackScheduler trackScheduler = null;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        super.onGuildReady(event);

        // Always join the voice channel named "Queue"
        var channels = event.getGuild().getVoiceChannels();
        VoiceChannel queueChannel = null;
        for (VoiceChannel channel : channels) {
            if (channel.getName().equals("Queue")) {
                queueChannel = channel;
            }
        }

        initVoiceChannel(event.getGuild().getAudioManager(), queueChannel);
    }

    private void initVoiceChannel(@NotNull AudioManager audioManager, VoiceChannel channel) {
        // Connect to channel
        audioManager.openAudioConnection(channel);

        // Players?
        playerManager = new CustomAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        player = playerManager.createPlayer();

        audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        audioManager.setSendingHandler(audioPlayerSendHandler);

        trackScheduler = new TrackScheduler();
        player.addListener(trackScheduler);
        player.setVolume(100);
    }

    private void initCountdown() throws InterruptedException, IOException {

        for (var i = 10; i >= 0; i--) {
            String resourcePath = this.getClass().getResource("/" + i + ".wav").getPath();
            if (this.getClass().getResource("VoiceChannelListener.class").toString().startsWith("jar")) {
                resourcePath = Paths.get("./" + i + ".wav").toAbsolutePath().toString();
            }

            System.out.println(resourcePath);


            loadItem(resourcePath, i);

        }
        Thread.sleep(1000);
        trackScheduler.start(player);
    }


    private InputStream getResourceInputStream(int number) {
        return this.getClass().getResourceAsStream("/" + number + ".m4a");

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // We don't want to respond to other bot accounts, including ourself
        var message = event.getMessage();
        var channel = event.getChannel();
        var content = message.getContentRaw();

        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping")) {
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }

        if (channel.getName().equals("mod") && content.equals("!queue")) {
            try {
                initCountdown();
//                initPoker();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private void initPoker() throws InterruptedException, IOException {
//        var file = this.getClass().getResourceAsStream("/poker.mp3");
//        System.out.println("filename.available() = " + file.available());
//
//        playFile(file, 1);
//        Thread.sleep(1000);
//        trackScheduler.start(player);
//
//    }

    private void playFile(InputStream resource, int order) throws IOException {
        var track = playerManager.decodeTrack(new MessageInput(resource)).decodedTrack;
        trackScheduler.queue(track, order);
    }

    private void loadItem(String identifier, int order) {
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        System.out.println("loadItem: trackLoaded");
                        trackScheduler.queue(track, order);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        System.out.println("loadItem: playlistLoaded");
                    }

                    @Override
                    public void noMatches() {
                        System.out.println("loadItem: noMatches");
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        System.out.println("loadItem: loadFailed");
                        exception.printStackTrace();
                    }
                }
        );
    }
}