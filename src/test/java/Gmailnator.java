import Http.ResponseCode;
import gmailnator.CookieHeaders;
import gmailnator.EndPoints;

import gmailnator.Utils;
import okhttp3.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.assertj.core.api.SoftAssertions;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.IOException;
import java.util.List;

public class Gmailnator {

    private OkHttpClient client;

    private ExtentReports report;

    private ExtentTest test;

    private ExtentHtmlReporter htmlReporter;

    private SoftAssertions softAssertion = new SoftAssertions();

    private static final String TEST_EMAIL = "dueltmp+yl3ko@gmail.com";

    @BeforeSuite
    public void init() {

        client = new OkHttpClient();
        report = new ExtentReports();
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "/test-output/STMExtentReport.html");

        report.attachReporter(htmlReporter);
        htmlReporter.config().setDocumentTitle("Gmailnator's REST APIs");
        htmlReporter.config().setReportName("report gmailnator");
        htmlReporter.config().setTheme(Theme.STANDARD);

        test = report.createTest("Gmailnator", "Gmailnator's REST APIs");

    }

    @Test
    public void generateEmail() throws IOException {

        ExtentTest testCase = test.createNode("generateEmail", "Generate a temporary email by gmailnator's REST APIs");

        Request request = new Request.Builder()
                .url(EndPoints.BASE_URL + "/")
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();

        softAssertion.assertThat(response.code() == ResponseCode.OK.getIntValue()).isTrue();

        if (response.code() == ResponseCode.OK.getIntValue()) {
            testCase.log(Status.PASS, "Access gmailnator's home page successfully.");
        } else {
            testCase.log(Status.FAIL, "Failed to access gmailnator's home page.");
        }


        List<String> cookies = response.headers().values(CookieHeaders.SET_COOKIE);
        String csrfGmailnatorToken = Utils.getCsrfGmailnatorCookie(cookies);

        RequestBody formBody = new FormBody.Builder()
                .add(CookieHeaders.CSRF_GMAILNATOR_TOKEN, csrfGmailnatorToken)
                .add(gmailnator.FormBody.ACTION, gmailnator.FormBody.GENERATE_EMAIL)
                .build();

        Request generateEmailRequest = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.GENERATE_EMAIL)
                .header(CookieHeaders.COOKIE, CookieHeaders.CSRF_GMAILNATOR_COOKIE + "=" + csrfGmailnatorToken)
                .post(formBody)
                .build();
        Call generateEmailCall = client.newCall(generateEmailRequest);

        Response generateEmailResponse = generateEmailCall.execute();

        String generatedEmail = generateEmailResponse.body().string();

        softAssertion.assertThat(generateEmailResponse.code() == ResponseCode.OK.getIntValue()).isTrue();

        if (generateEmailResponse.code() == ResponseCode.OK.getIntValue()) {
            testCase.log(Status.PASS, "Generated email from Gmailnator: " + generatedEmail);
        } else {
            testCase.log(Status.FAIL, "An error has occurred. Cannot generate email.");
        }


    }

    @Test
    public void getSingleMessage() throws IOException {

        ExtentTest testCase = test.createNode("getSingleMessage", "Get a single message from a provided email address.");

        Request request = new Request.Builder()
                .url(EndPoints.BASE_URL + EndPoints.INBOX)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();

        softAssertion.assertThat(response.code() == ResponseCode.OK.getIntValue()).isTrue();

        if (response.code() == ResponseCode.OK.getIntValue()) {
            testCase.log(Status.PASS, "Access mail box successfully.");
        } else {
            testCase.log(Status.FAIL, "Failed to access mailbox.");
        }

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

        softAssertion.assertThat(loadMailListResponse.code() == ResponseCode.OK.getIntValue()).isTrue();

        if (loadMailListResponse.code() == ResponseCode.OK.getIntValue()) {
            testCase.log(Status.PASS, "Loading mail list successfully.");
        } else {
            testCase.log(Status.FAIL, "Failed to load mail list.");
        }

        String loadMailListResponseBody = loadMailListResponse.body().string();

        loadMailListResponseBody = loadMailListResponseBody.replace("\"", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("\\", "");

        String emailServer = loadMailListResponseBody.substring(loadMailListResponseBody.indexOf(EndPoints.BASE_URL) + EndPoints.BASE_URL.length() + 1, loadMailListResponseBody.indexOf(EndPoints.MESSAGE_ID));
        int beginIndex = loadMailListResponseBody.indexOf(EndPoints.MESSAGE_ID) + EndPoints.MESSAGE_ID.length() + 1;
        String messageId = loadMailListResponseBody.substring(beginIndex, beginIndex + 16);

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

        String getSingleMessageResponseBody = getSingleMessageResponse.body().string();

        softAssertion.assertThat(getSingleMessageResponse.code() == ResponseCode.OK.getIntValue()).isTrue();

        if (loadMailListResponse.code() == ResponseCode.OK.getIntValue()) {
            testCase.log(Status.PASS, "Getting single message successfully.");
        } else {
            testCase.log(Status.FAIL, "Failed to get single message.");
        }

    }

    @AfterMethod
    public void testTearDown() {
        softAssertion.assertAll();
    }

    @AfterSuite
    public void afterSuite() {
        report.flush();
    }
}
