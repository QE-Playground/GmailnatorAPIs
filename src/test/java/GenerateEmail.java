import Http.ResponseCode;
import gmailnator.CookieHeaders;
import gmailnator.EndPoints;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import gmailnator.Utils;
import okhttp3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.IOException;
import java.util.List;

public class GenerateEmail {

    private OkHttpClient client;

    private ExtentReports report;

    private ExtentTest test;

    private ExtentHtmlReporter htmlReporter;

    @Before
    public void init() {

        client = new OkHttpClient();
        report = new ExtentReports();
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "/test-output/STMExtentReport.html");

        report.attachReporter(htmlReporter);
        htmlReporter.config().setDocumentTitle("Gmailnator's REST APIs");
        htmlReporter.config().setReportName("report gmailnator");
        htmlReporter.config().setTheme(Theme.STANDARD);

        test = report.createTest("generateEmail", "Generate a temporary email by gmailnator's REST APIs");

    }

    @Test
    public void generateEmail() throws IOException {

        Request request = new Request.Builder()
                .url(EndPoints.BASE_URL + "/")
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();

        assertThat(response.code(), equalTo(ResponseCode.OK));

        test.log(Status.PASS, "Access to gmailnator's home page successfully.");

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

        assertThat(generateEmailResponse.code(), equalTo(ResponseCode.OK));

        test.log(Status.PASS, "Temporary email is generated successfully.");

        String generatedEmail = generateEmailResponse.body().string();

        System.out.println("Generated email from Gmailnator: " + generatedEmail);
    }

    @After
    public void tearDown() {

        report.flush();

    }
}
