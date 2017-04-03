package com.vauff.maunzdiscord.commands;

import com.vauff.maunzdiscord.core.ICommand;
import com.vauff.maunzdiscord.core.Util;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Ramon on 03-Apr-17.
 */
public class IsItDown implements ICommand<MessageReceivedEvent> {
    @Override
    public void exe(MessageReceivedEvent event) throws Exception {
        String[] args = event.getMessage().getContent().split(" ");

        if (args.length == 2) {
            boolean isUp;
            String hostname = args[2].replaceAll("^https?:\\/\\/", "").split("/")[0];
            if (args[2].startsWith("https")) {
                isUp = pingHost(hostname, 443, 4000);
            } else {
                isUp = pingHost(hostname, 80, 4000);
            }
            Util.msg(event.getMessage().getChannel(), hostname + " is currently " + (isUp ? "UP" : "DOWN") + ".");
        } else {
            Util.msg(event.getMessage().getChannel(), "Usage: *isitdown <hostname>");
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"isdown", "isitdown", "isup", "isitup"};
    }

    private static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
