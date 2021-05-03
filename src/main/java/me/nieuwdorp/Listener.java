package me.nieuwdorp;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Listener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        super.onGuildReady(event);

        extracted(event.getGuild().getAudioManager(), event.getGuild().getVoiceChannels().get(0));
    }

    private void extracted(@NotNull AudioManager audioManager, VoiceChannel channel) {
        // Get channel

        // Connect to channel
        audioManager.openAudioConnection(channel);

        // Players?so
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerLocalSource(playerManager);
        AudioPlayer player = playerManager.createPlayer();


        AudioPlayerSendHandler audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        audioManager.setSendingHandler(audioPlayerSendHandler);

        TrackScheduler trackScheduler = new TrackScheduler();
        player.addListener(trackScheduler);

        playerManager.loadItem("src/main/resources/audio-numbers/0.mp3", new AudioLoadResultHandler() {

                    @Override
                    public void trackLoaded(AudioTrack track) {
                        System.out.println("trackLoaded");
                        trackScheduler.queue(player, track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        System.out.println("playlistLoaded");

                    }

                    @Override
                    public void noMatches() {
                        System.out.println("noMatches");

                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        System.out.println("failed");
                        System.out.println(exception.getMessage());

                    }
                }
        );
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        extracted(event.getGuild().getAudioManager(), event.getGuild().getVoiceChannels().get(0));
        if (event.getAuthor().isBot()) return;

        // We don't want to respond to other bot accounts, including ourself
        var message = event.getMessage();
        var content = message.getContentRaw();

        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
    }
}