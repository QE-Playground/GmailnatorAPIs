import Http.ResponseCode;
import gmailnator.CookieHeaders;
import gmailnator.EndPoints;
import gmailnator.Utils;
import okhttp3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GetSingleMessage {

    OkHttpClient client;

    private static final String TEST_EMAIL = "foomtmp+3z7yf@gmail.com";

    @Before
    public void init() {

        client = new OkHttpClient();
    }

    @Test
    public void getSingleMessage() throws IOException {

        Request request = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.INBOX)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();

        assertThat(response.code(), equalTo(ResponseCode.OK));

        List<String> cookies = response.headers().values(CookieHeaders.SET_COOKIE);
        String csrfGmailnatorToken = Utils.getCsrfGmailnatorCookie(cookies);

        RequestBody formBody = new FormBody.Builder()
                .add(CookieHeaders.CSRF_GMAILNATOR_TOKEN, csrfGmailnatorToken)
                .add(gmailnator.FormBody.ACTION, gmailnator.FormBody.LOAD_MAIL_LIST)
                .add(gmailnator.FormBody.EMAIL_ADDRESS, Utils.getEncodedEmail(TEST_EMAIL))
                .build();

        Request loadMailListRequest = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.MAILBOX_QUERY)
                .header(CookieHeaders.COOKIE, CookieHeaders.CSRF_GMAILNATOR_COOKIE + "=" + csrfGmailnatorToken)
                .post(formBody)
                .build();
        Call loadMailListCall = client.newCall(loadMailListRequest);

        Response loadMailListResponse = loadMailListCall.execute();

        assertThat(loadMailListResponse.code(), equalTo(ResponseCode.OK));

        String loadMailListResponseBody = loadMailListResponse.body().string();

        loadMailListResponseBody = loadMailListResponseBody.replace("\"", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("\\", "");

        String emailServer = loadMailListResponseBody.substring(loadMailListResponseBody.indexOf(EndPoints.BASE_URL) + EndPoints.BASE_URL.length() + 1, loadMailListResponseBody.indexOf(EndPoints.MESSAGE_ID));
        String messageId = loadMailListResponseBody.substring(loadMailListResponseBody.indexOf(EndPoints.MESSAGE_ID) + EndPoints.MESSAGE_ID.length() + 1, loadMailListResponseBody.indexOf("<table") - 7);

        RequestBody formBodyGetSingleMessage = new FormBody.Builder()
                .add(CookieHeaders.CSRF_GMAILNATOR_TOKEN, csrfGmailnatorToken)
                .add(gmailnator.FormBody.ACTION, gmailnator.FormBody.GET_MESSAGE)
                .add(gmailnator.FormBody.EMAIL, emailServer)
                .add(gmailnator.FormBody.MESSAGE_ID, messageId)
                .build();

        Request getSingleMessageRequest = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.GET_SINGLE_MESSAGE)
                .header(CookieHeaders.COOKIE, CookieHeaders.CSRF_GMAILNATOR_COOKIE + "=" + csrfGmailnatorToken)
                .post(formBodyGetSingleMessage)
                .build();
        Call getSingleMessageCall = client.newCall(getSingleMessageRequest);

        Response getSingleMessageResponse = getSingleMessageCall.execute();

        assertThat(getSingleMessageResponse.code(), equalTo(ResponseCode.OK));

        String getSingleMessageResponseBody = getSingleMessageResponse.body().string();

        System.out.println(getSingleMessageResponseBody);
    }
}
