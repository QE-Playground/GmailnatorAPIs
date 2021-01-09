import gmailnator.CookieHeaders;
import gmailnator.EndPoints;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import gmailnator.Utils;
import okhttp3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class GenerateEmail {

    OkHttpClient client;

    @Before
    public void init() {

        client = new OkHttpClient();
    }

    @Test
    public void generateEmail() throws IOException {

        Request request = new Request.Builder()
                .url(EndPoints.BASE_URL + "/")
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();

        assertThat(response.code(), equalTo(200));

        List<String> cookies = response.headers().values("Set-Cookie");
        String csrfGmailnatorToken = Utils.getCsrfGmailnatorCookie(cookies);

        RequestBody formBody = new FormBody.Builder()
                .add(CookieHeaders.CSRF_GMAILNATOR_TOKEN, csrfGmailnatorToken)
                .add(gmailnator.FormBody.ACTION, gmailnator.FormBody.GENERATE_EMAIL)
                .build();

        Request generateEmailRequest = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.GENERATE_EMAIL)
                .header("Cookie", CookieHeaders.CSRF_GMAILNATOR_COOKIE + "=" + csrfGmailnatorToken)
                .post(formBody)
                .build();
        Call generateEmailCall = client.newCall(generateEmailRequest);

        Response generateEmailResponse = generateEmailCall.execute();

        assertThat(response.code(), equalTo(200));

        String generatedEmail = generateEmailResponse.body().string();

        System.out.println("Generated email from Gmailnator: " + generatedEmail);
    }
}
