package org.bukkit.event.player;

import java.net.InetAddress;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Stores details for players attempting to log in.
 * <p>
 * This event is asynchronous, and not run using main thread.
 */
public class AsyncPlayerPreLoginEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Result result;
    private String message;
    //private final String name; // PandaSpigot - Not used anymore
    private final InetAddress ipAddress;
    //private final UUID uniqueId; // PandaSpigot - Not used anymore

    @Deprecated
    public AsyncPlayerPreLoginEvent(final String name, final InetAddress ipAddress) {
        this(name, ipAddress, null);
    }

    // PandaSpigot start - Ability to change PlayerProfile in AsyncPreLoginEvent
    public AsyncPlayerPreLoginEvent(final String name, final InetAddress ipAddress, final UUID uniqueId) {
        this(name, ipAddress, uniqueId, org.bukkit.Bukkit.createProfile(uniqueId, name));
    }
    private com.destroystokyo.paper.profile.PlayerProfile profile;

    /**
     * Gets the PlayerProfile of the player logging in
     * @return The Profile
     */
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return profile;
    }

    /**
     * Changes the PlayerProfile the player will login as
     * @param profile The profile to use
     */
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        this.profile = profile;
    }

    public AsyncPlayerPreLoginEvent(final String name, final InetAddress ipAddress, final UUID uniqueId, final com.destroystokyo.paper.profile.PlayerProfile profile) {
        super(true);
        this.profile = profile;
    // PandaSpigot end
        this.result = Result.ALLOWED;
        this.message = "";
        //this.name = name; // PandaSpigot - Not used anymore
        this.ipAddress = ipAddress;
        //this.uniqueId = uniqueId; // PandaSpigot - Not used anymore
    }

    /**
     * Gets the current result of the login, as an enum
     *
     * @return Current Result of the login
     */
    public Result getLoginResult() {
        return result;
    }

    /**
     * Gets the current result of the login, as an enum
     *
     * @return Current Result of the login
     * @deprecated This method uses a deprecated enum from {@link
     *     PlayerPreLoginEvent}
     * @see #getLoginResult()
     */
    @Deprecated
    public PlayerPreLoginEvent.Result getResult() {
        return result == null ? null : result.old();
    }

    /**
     * Sets the new result of the login, as an enum
     *
     * @param result New result to set
     */
    public void setLoginResult(final Result result) {
        this.result = result;
    }

    /**
     * Sets the new result of the login, as an enum
     *
     * @param result New result to set
     * @deprecated This method uses a deprecated enum from {@link
     *     PlayerPreLoginEvent}
     * @see #setLoginResult(Result)
     */
    @Deprecated
    public void setResult(final PlayerPreLoginEvent.Result result) {
        this.result = result == null ? null : Result.valueOf(result.name());
    }

    /**
     * Gets the current kick message that will be used if getResult() !=
     * Result.ALLOWED
     *
     * @return Current kick message
     */
    public String getKickMessage() {
        return message;
    }

    /**
     * Sets the kick message to display if getResult() != Result.ALLOWED
     *
     * @param message New kick message
     */
    public void setKickMessage(final String message) {
        this.message = message;
    }

    /**
     * Allows the player to log in
     */
    public void allow() {
        result = Result.ALLOWED;
        message = "";
    }

    /**
     * Disallows the player from logging in, with the given reason
     *
     * @param result New result for disallowing the player
     * @param message Kick message to display to the user
     */
    public void disallow(final Result result, final String message) {
        this.result = result;
        this.message = message;
    }

    /**
     * Disallows the player from logging in, with the given reason
     *
     * @param result New result for disallowing the player
     * @param message Kick message to display to the user
     * @deprecated This method uses a deprecated enum from {@link
     *     PlayerPreLoginEvent}
     * @see #disallow(Result, String)
     */
    @Deprecated
    public void disallow(final PlayerPreLoginEvent.Result result, final String message) {
        this.result = result == null ? null : Result.valueOf(result.name());
        this.message = message;
    }

    /**
     * Gets the player's name.
     *
     * @return the player's name
     */
    public String getName() {
        return profile.getName(); // PandaSpigot - Use profile
    }

    /**
     * Gets the player IP address.
     *
     * @return The IP address
     */
    public InetAddress getAddress() {
        return ipAddress;
    }

    /**
     * Gets the player's unique ID.
     *
     * @return The unique ID
     */
    public UUID getUniqueId() {
        return profile.getId(); // PandaSpigot - Use profile
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Basic kick reasons for communicating to plugins
     */
    public enum Result {

        /**
         * The player is allowed to log in
         */
        ALLOWED,
        /**
         * The player is not allowed to log in, due to the server being full
         */
        KICK_FULL,
        /**
         * The player is not allowed to log in, due to them being banned
         */
        KICK_BANNED,
        /**
         * The player is not allowed to log in, due to them not being on the
         * white list
         */
        KICK_WHITELIST,
        /**
         * The player is not allowed to log in, for reasons undefined
         */
        KICK_OTHER;

        @Deprecated
        private PlayerPreLoginEvent.Result old() {
            return PlayerPreLoginEvent.Result.valueOf(name());
        }
    }
}
