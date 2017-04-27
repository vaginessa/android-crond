package it.faerb.crond;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "crond.MainActivity";
    private static final String CRONTAB_FILE = "/data/local/spool/cron/crontabs/root";
    private static final String CROND_LOG_FILE = "/data/crond.log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView crontabLabel = (TextView) findViewById(R.id.text_label_crontab);
        crontabLabel.setText(String.format("crontab %s:", CRONTAB_FILE));

        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        crontabContent.setText(displayFileContents(CRONTAB_FILE));
        crontabContent.setMovementMethod(new ScrollingMovementMethod());

        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond);
        crondLog.setText(displayFileContents(CROND_LOG_FILE));
        crondLog.setMovementMethod(new ScrollingMovementMethod());

        final Button restartButton = (Button) findViewById(R.id.button_restart_crond);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, executeCommand(
                                "ps | grep \"root.*crond\" | awk '{print $2}' | xargs kill"));
                Log.i(TAG, executeCommand(
                        "crond -L /data/crond.log -l 7"));
            }
        });
    }

    private String displayFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    private String executeCommand(String command) {
        return executeCommand(new String[]{"su", "-c", command, "-"});
    }

    private String executeCommand(String[] command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
