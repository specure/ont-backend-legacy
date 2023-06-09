package at.alladin.rmbt.client.helper;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * based on this discussion:
 * https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
 */


public class BrowserShow {

    public static String MEASUREMENT_SYSTEM_BASE_URL = "https://ont.specure.com/en/history/";

    public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static OS_TYPE getOsType() {
        String osName = getOSName();
        if (osName.indexOf("win") >= 0)
            return OS_TYPE.WINDOWS;
        if (osName.indexOf("mac") >= 0)
            return OS_TYPE.MACOS;
        if (osName.indexOf("nix") >= 0)
            return OS_TYPE.UNIX;
        if (osName.indexOf("nux") >= 0)
            return OS_TYPE.LINUX;
        return OS_TYPE.UNKNOWN;
    }

    private static void showOnWindows(String url) {

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                System.err.println("Unable to show results in Windows browser. Reason:");
                //e.printStackTrace();
                System.err.println(e.getMessage());
            }
        } else {
            Runtime rt = Runtime.getRuntime();
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e) {
                System.err.println("Unable to show results in Windows browser. Reason:");
                //e.printStackTrace();
                System.err.println(e.getMessage());
            }
            //System.err.println("Unable to show results in Windows browser. Desktop is not supported.");
        }

    }

    private static void showOnMacBook(String url) {
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("open " + url);
        } catch (IOException e) {
            System.err.println("Unable to show results in MacOS browser. Reason:");
            //e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    private static void showOnLinux(String url) {
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("xdg-open " + url);
        } catch (IOException e) {
            System.err.println("Unable to show results in Linux/Unix browser. Reason:");
            //e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public static void showResults(String url) {

        switch (getOsType()) {
            case WINDOWS:
                showOnWindows(url);
                break;
            case MACOS:
                showOnMacBook(url);
                break;

            case LINUX:
                showOnLinux(url);
                break;
            case UNIX:
                showOnLinux(url);
                break;
            case UNKNOWN:
                System.err.println("Unable to show results in browser. Unknown Operating system. You can manually view results in browser pointing to url: " + url);
                break;

        }
    }

    public enum OS_TYPE {
        WINDOWS, MACOS, UNIX, LINUX, UNKNOWN;
    }
}
