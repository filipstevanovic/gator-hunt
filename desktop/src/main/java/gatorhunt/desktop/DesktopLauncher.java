package gatorhunt.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import gatorhunt.GatorHuntGame;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class DesktopLauncher {

    public static void main(String[] args) {
        if (relaunchedWithStartOnFirstThread()) {
            return;
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Gator Hunt");
        config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.useVsync(true);
        config.setForegroundFPS(70);

        new Lwjgl3Application(new GatorHuntGame(), config);
    }

    /**
     * LWJGL/GLFW requires macOS to run its event loop on the process's very
     * first thread, via the -XstartOnFirstThread JVM flag. That flag can
     * only be set at JVM startup, so it doesn't help to set it here -- and
     * whether it's already set depends entirely on how this was launched.
     * Gradle's desktop:run task adds it (see desktop/build.gradle), but an
     * IDE's "run main()" action generates its own run configuration that
     * bypasses that, and crashes with "GLFW may only be used on the main
     * thread". Rather than depend on every possible launcher remembering
     * the flag, detect its absence here and relaunch in a new JVM that has
     * it, the same trick LWJGL's own samples use.
     *
     * @return true if this process relaunched and the caller should just return.
     */
    private static boolean relaunchedWithStartOnFirstThread() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            return false;
        }
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XstartOnFirstThread")) {
            return false;
        }

        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-XstartOnFirstThread");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(DesktopLauncher.class.getName());

        try {
            Process process = new ProcessBuilder(command).inheritIO().start();
            process.waitFor();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to relaunch with -XstartOnFirstThread", e);
        }
        return true;
    }
}
