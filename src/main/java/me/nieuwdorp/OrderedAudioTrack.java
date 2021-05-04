package me.nieuwdorp;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface OrderedAudioTrack extends AudioTrack {
    Integer order = null;
}
