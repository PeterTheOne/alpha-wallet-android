package io.stormbird.token.tools;

/**
 * <h4>
 * SimpleLambdaMessage
 * </h4>
 * <p>
 * A class for sending message strings to an Amazon Web Services Lambda Function
 * </p>
 * <p>
 * Feb 19, 2018
 * </p>
 *
 * @author Ian Kaplan, iank@bearcave.com
 */
public class SimpleLambdaMessage {
    private String mMessage;

    /**
     * @param message the message contents for the object
     */
    public void setMessage(final String message) {
        this.mMessage = message;
    }

    /**
     * @return the message string
     */
    public String getMessage() {
        return this.mMessage;
    }

    @Override
    public String toString() {
        return this.mMessage;
    }

}
