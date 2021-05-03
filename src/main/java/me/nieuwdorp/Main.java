package me.nieuwdorp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = JDABuilder
                .createDefault("ODM4ODc1ODQ4MzY1OTY1MzUy.YJBeAw.PbpgVWGaG6EdUMjgQv-CHzr4jdo")
                .addEventListeners(new Listener())
                .build()
                .awaitReady();
    }
}
