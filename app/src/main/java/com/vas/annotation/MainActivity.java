package com.vas.annotation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.vas.annotation.model.SampleObject_;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SampleObject_ sampleObject_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.log);
        SampleObject_.fetch("H5gqBrgC5u", new GetCallback<SampleObject_>() {
            @Override
            public void done(SampleObject_ object, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    textView.append("\n FETCH FAIL: " + e.getMessage());
                } else {
                    Log.i("FETCH", "SUCCESSFUL: " + object.toString());
                    textView.append("\n FETCH SUCCESSFUL: " + object.toString());
                }
            }
        });
        sampleObject_ = new SampleObject_();
        sampleObject_.setFieldString("TESTE");
        sampleObject_.setFieldBytes("TESTE_BYTES".getBytes());
        sampleObject_.setFieldParseFile(new ParseFile("TESTE_FILE", "TESTE_FILE".getBytes()));
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sampleObject_.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                            textView.append("\n SAVE FAIL: " + e.getMessage());
                        } else {
                            Log.i("SAVE", "SUCCESSFUL: " + sampleObject_.toString());
                            textView.append("\n SAVE SUCCESSFUL: " + sampleObject_.toString());
                        }
                    }
                });
            }
        });
        findViewById(R.id.query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SampleObject_.query().findInBackground(new FindCallback<SampleObject_>() {
                    @Override
                    public void done(List<SampleObject_> objects, ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                            textView.append("\n QUERY FAIL: " + e.getMessage());
                        } else {
                            Log.i("QUERY", Arrays.toString(objects.toArray()));
                            textView.append("\n QUERY SUCCESSFUL: " + Arrays.toString(objects.toArray()));
                        }
                    }
                });
            }
        });
    }
}
