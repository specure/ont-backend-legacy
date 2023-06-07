package at.alladin.rmbt.qosadmin.util;

public class IllegalInputException extends Exception {
    private static final long serialVersionUID = 1L;

    public IllegalInputException() {
        super();
    }

    public IllegalInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalInputException(String message) {
        super(message);
    }

    public IllegalInputException(Throwable cause) {
        super(cause);
    }

}
