package me.nieuwdorp;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

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
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        player = playerManager.createPlayer();

        audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        audioManager.setSendingHandler(audioPlayerSendHandler);

        trackScheduler = new TrackScheduler();
        player.addListener(trackScheduler);
        player.setVolume(100);
    }

    private void initCountdown() throws InterruptedException {
        for (var i = 10; i >= 0; i--) {
            var filename = constructCountdownFilepath(i);
            playFile(filename, i);
        }
        Thread.sleep(1000);
        trackScheduler.start(player);
    }

    private String constructCountdownFilepath(int number) {
        return this.getClass().getClassLoader().getResource(number + ".wav").getPath();
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void playFile(String identifier, int order) {
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