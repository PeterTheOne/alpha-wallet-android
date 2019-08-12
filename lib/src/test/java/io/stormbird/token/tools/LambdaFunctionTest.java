package io.stormbird.token.tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.protocol.json.SdkJsonGenerator.JsonGenerationException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.model.Region;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple test harness for invoking your Lambda function via a remote Lambda call.
 */
public class LambdaFunctionTest {

    /**
     * Build an Amazon Web Services Lambda client object.
     *
     * @param accessID The AWS credential ID
     * @param accessKey The AWS credential secret key
     * @return An AWS Lambda client
     */
    private AWSLambda buildClient(String accessID, String accessKey) {
        Regions region = Regions.fromName("cn-north-1");
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessID, accessKey);
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region);
        AWSLambda client = builder.build();
        return client;
    }

    /**
     * See http://www.baeldung.com/jackson-inheritance
     *
     * @param obj
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    private static String objectToJSON( Object obj, Logger logger) {
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(obj);
        } catch (JsonGenerationException | IOException e) {
            logger.severe("Object to JSON failed: " + e.getLocalizedMessage());
        }
        return json;
    }

    @Test
    public void testLambdaFunction() {
        final String aws_access_key_id = "AKIATTNYKZHVXF23T44J";
        final String aws_secret_access_key = "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";
        final Logger logger = Logger.getLogger( this.getClass().getName() );
        final String messageToLambda = "Hello Lambda Function";
        final SimpleLambdaMessage messageObj = new SimpleLambdaMessage();
        messageObj.setMessage(messageToLambda);

        AWSLambda lambdaClient = buildClient(aws_access_key_id, aws_secret_access_key);
        String lambdaMessageJSON = objectToJSON( messageObj, logger );
        InvokeRequest req = new InvokeRequest()
                .withFunctionName(SimpleLambdaFunctionExample.lambdaFunctionName)
                .withPayload( lambdaMessageJSON );
        InvokeResult requestResult = lambdaClient.invoke(req);
        ByteBuffer byteBuf = requestResult.getPayload();
        if (byteBuf != null) {
            String result = StandardCharsets.UTF_8.decode(byteBuf).toString();
            logger.info("testLambdaFunction::Lambda result: " + result);
        } else {
            logger.severe("testLambdaFunction: result payload is null");
        }
    }
}
