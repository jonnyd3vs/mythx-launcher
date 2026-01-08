package mythx.launcher.exception;

public class LauncherException extends Exception {
    public LauncherException() {
    }

    public LauncherException(String message) {
        super(message);
    }

    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
}
