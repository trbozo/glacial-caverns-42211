package com.heroku.java;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@Controller
public class GettingStartedApplication extends ListenerAdapter {
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(GettingStartedApplication.class);

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Bot successfully connected and ready to receive commands!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals("!help")) {
            event.getChannel().sendMessage("Hello, I am a Discord bot built with Java!").queue();
        }
    }

    public static void main(String[] args) {
        try {
            JDABuilder.createDefault("YOUR_BOT_TOKEN")
                    .addEventListeners(new GettingStartedApplication())
                    .setActivity(Activity.playing("Type !help"))
                    .build();
            SpringApplication.run(GettingStartedApplication.class, args);
        } catch (Exception e) {
            logger.error("An error occurred while starting the bot", e);
        }
    }
}
