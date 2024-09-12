import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    private static final String CONFIG_FILE = "../config.ini";

    public static int readPortNumber() {
        return readIntegerProperty("port");
    }

    public static String readRootDirectory() {
        return readStringProperty("root");
    }

    public static String readDefaultPage() {
        return readStringProperty("defaultPage");
    }

    public static int readMaxThreads() {
        return readIntegerProperty("maxThreads");
    }

    private static int readIntegerProperty(String propertyName) {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            props.load(fis);
            return Integer.parseInt(props.getProperty(propertyName));
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }

    private static String readStringProperty(String propertyName) {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            props.load(fis);
            return props.getProperty(propertyName);
        } catch (Exception e) {
            e.printStackTrace();
            return null; 
        }
    }
}
