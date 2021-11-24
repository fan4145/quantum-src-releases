package me.derp.quantum.features.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.derp.quantum.features.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public
class QueueCommand extends Command {
    public
    QueueCommand() {
        super("queue", new String[]{"priority, regular"});
    }

    @Override
    public
    void execute(final String[] commands) {
        if (commands.length == 1 || commands.length == 0) {
            QueueCommand.sendMessage("ayo, specify the type! (priority/regular)");
            return;
        }

        // Connect to the URL using java's native library
        String check = commands[0];
        if (check.equalsIgnoreCase("regular")) {
            try {
                HttpURLConnection request = (HttpURLConnection) new URL("https://2bqueue.info/*").openConnection();
                request.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                request.connect();
                JsonParser jp = new JsonParser(); //from gson
                JsonElement root = jp.parse(new BufferedReader(new InputStreamReader(request.getInputStream()))); //Convert the input stream to a json element
                JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
                String aaaaaa = rootobj.get("regular").getAsString(); //just grab the zipcode
                QueueCommand.sendMessage("Regular queue currently have: " + aaaaaa);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (check.equalsIgnoreCase("priority")) {
            try {
                HttpURLConnection request = (HttpURLConnection) new URL("https://2bqueue.info/*").openConnection();
                request.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                request.connect();
                JsonParser jp = new JsonParser(); //from gson
                JsonElement root = jp.parse(new BufferedReader(new InputStreamReader(request.getInputStream()))); //Convert the input stream to a json element
                JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
                String aaaaaa = rootobj.get("prio").getAsString(); //just grab the zipcode
                QueueCommand.sendMessage("Priority queue currently have: " + aaaaaa);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}