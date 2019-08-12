package io.stormbird.token.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context;

// this lambda function capitalises the input.
public class SimpleLambdaFunctionExample implements RequestStreamHandler{
    public final static String lambdaFunctionName = "Express-Of-Trust";

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        int letter;
        while((letter = inputStream.read()) != -1)
        {
            outputStream.write(Character.toUpperCase(letter));
        }
    }
}