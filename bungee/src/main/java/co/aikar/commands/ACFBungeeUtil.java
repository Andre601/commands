/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import co.aikar.locales.MessageKey;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ACFBungeeUtil {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Move to Message Keys on the CommandIssuer
     * @deprecated
     */
    @Deprecated
    public static void sendMsg(CommandSender player, String message) {
        message = color(message);
        for (String msg : ACFPatterns.NEWLINE.split(message)) {
            player.sendMessage(msg);
        }
    }

    public static String removeColors(String msg) {
        return ChatColor.stripColor(color(msg));
    }

    public static String replaceChatString(String message, String replace, String with) {
        return replaceChatString(message, Pattern.compile(Pattern.quote(replace), Pattern.CASE_INSENSITIVE), with);
    }

    public static String replaceChatString(String message, Pattern replace, String with) {
        final String[] split = replace.split(message + "1");

        if (split.length < 2) {
            return replace.matcher(message).replaceAll(with);
        }
        message = split[0];

        for (int i = 1; i < split.length; i++) {
            final String prev = getLastColors(message);
            message += with + prev + split[i];
        }
        return message.substring(0, message.length() - 1);
    }

    //Imported from org.bukkit.ChatColor

    public static final char COLOR_CHAR = '\u00A7';

    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    result = color.toString() + result;

                    // Once we find a color or reset we can stop searching
                    if (isChatColorAColor(color) || color.equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static boolean isChatColorAColor(ChatColor chatColor) {
        return chatColor != ChatColor.MAGIC && chatColor != ChatColor.BOLD
                && chatColor != ChatColor.STRIKETHROUGH && chatColor != ChatColor.UNDERLINE
                && chatColor != ChatColor.ITALIC;
    }


    public static ProxiedPlayer findPlayerSmart(CommandIssuer issuer, String search) {
        CommandSender requester = issuer.getIssuer();
        String name = ACFUtil.replace(search, ":confirm", "");
        if (name.length() < 3) {
            issuer.sendError(BungeeMessageKeys.USERNAME_TOO_SHORT);
            return null;
        }
        if (!isValidName(name)) {
            issuer.sendError(BungeeMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<ProxiedPlayer> matches = new ArrayList<>(ProxyServer.getInstance().matchPlayer(name));

        if (matches.size() > 1) {
            String allMatches = matches.stream().map(ProxiedPlayer::getName).collect(Collectors.joining(", "));
            issuer.sendError(BungeeMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        if (matches.isEmpty()) {
            issuer.sendError(BungeeMessageKeys.NO_PLAYER_FOUND_SERVER,
                    "{search}", name);
            return null;
        }

        return matches.get(0);
    }

    /**
     * Please move to the CommandIssuer version
     * @deprecated
     */
    @Deprecated
    public static ProxiedPlayer findPlayerSmart(CommandSender requester, String search) {
        String name = ACFUtil.replace(search, ":confirm", "");
        if (name.length() < 3) {
            requester.sendMessage("§cUsername too short, must be at least three characters");
            return null;
        }
        if (!isValidName(name)) {
            requester.sendMessage("§c'" + name + "' is not a valid username");
            return null;
        }

        List<ProxiedPlayer> matches = new ArrayList<>(ProxyServer.getInstance().matchPlayer(name));

        if (matches.size() > 1) {
            requester.sendMessage("§cMultiple players matched '" + name + "', please be more specific");
            return null;
        }

        if (matches.isEmpty()) {
            requester.sendMessage("§cNo player matching '" + name + "' is connected to this server");
            return null;
        }

        return matches.get(0);
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    public static <T> T validate(T object, String message, Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
        return object;
    }


}
