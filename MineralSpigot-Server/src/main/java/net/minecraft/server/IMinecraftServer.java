package net.minecraft.server;

public interface IMinecraftServer {

    String b();

    String E();

    int F();

    String G();

    String getVersion();

    int I();

    int J();

    String[] getPlayers();

    String U();

    String getPlugins();

    String executeRemoteCommand(String s);

    boolean isDebugging();

    void info(String s);

    void warning(String s);

    void g(String s);

    void h(String s);
}
