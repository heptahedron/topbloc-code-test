package codes.braxton.topbloc;

import com.github.kevinsawicki.http.HttpRequest;

import java.util.Scanner;

import codes.braxton.topbloc.TestTable;
import codes.braxton.topbloc.TestTableException;

public class App {

    public static void main(String[] args) throws TestTableException {
        if (args.length < 4) {
            System.out.println("Usage: topblocapp <table_a_path> <table_2_path> <email> <application_url>");
            System.exit(1);
        } 

        String table1Path = args[0];
        String table2Path = args[1];
        String email      = args[2];
        String appUrl     = args[3];

        TestTable t1 = TestTable.readFrom(table1Path);
        TestTable t2 = TestTable.readFrom(table2Path);
        TestTable t3 = TestTable.combine(t1, t2);

        String appContent = t3.createRequestJson(email);

        System.out.println("Table 1 contents:");
        t1.printValues();
        System.out.println();

        System.out.println("Table 2 contents:");
        t2.printValues();
        System.out.println();

        System.out.println("Combined table contents:");
        t3.printValues();
        System.out.println();

        System.out.println("Application content:");
        System.out.println(appContent);
        System.out.println();
        
        System.out.println("Application url: " + appUrl);

        if (confirm("Send application? [y/n] ", "y")) {
            String response;

            System.out.println("Sending application...");
            try {
                response = sendApplication(appUrl, appContent);
            } catch (Exception e) {
                System.out.println("Error while sending application: ");
                e.printStackTrace();
                System.exit(2);
                return;
            }

            System.out.println("Application sent.");
            System.out.println("Response:");
            System.out.println(response);
        } else {
            System.out.println("Aborted.");
            System.exit(3);
        }
    }

    private static boolean confirm(String prompt, String yes) {
        System.out.print(prompt);
        
        String response = new Scanner(System.in).next();

        return response.equals(yes);
    }

    private static String sendApplication(String url, String appContent) {
        return HttpRequest.post(url)
            .contentType(HttpRequest.CONTENT_TYPE_JSON)
            .send(appContent)
            .body();
    }

}
