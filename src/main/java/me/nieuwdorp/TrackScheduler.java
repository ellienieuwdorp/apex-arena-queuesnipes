package me.nieuwdorp;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

public class TrackScheduler extends AudioEventAdapter {
    private final HashMap<Integer, AudioTrack> queue = new HashMap<>();

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            // Start next track
            if (!queue.isEmpty()) {
                player.playTrack(queue.remove(getHighestOrder()));
            }
        }

        System.out.println("TrackScheduler: onTrackEnd, notMayStartNext");
        System.out.println("endReason = " + endReason);

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        System.out.println("TrackScheduler: onTrackException");
        exception.printStackTrace();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        System.out.println("TrackScheduler: onTrackStuck");
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    public void queue(AudioTrack track, int order) {
        System.out.println("TrackScheduler: queue");
        queue.put(order, track);
    }

    public void start(AudioPlayer audioPlayer) {
        System.out.println("TrackScheduler: start");

        audioPlayer.playTrack(queue.remove(getHighestOrder()));
    }

    private Integer getHighestOrder() {
        Integer highest = null;
        for (Map.Entry<Integer, AudioTrack> entry : queue.entrySet()) {
            if (highest == null) {
                highest = entry.getKey();
            }
            if (highest < entry.getKey()) {
                highest = entry.getKey();
            }
        }
        return highest;
    }
}
