package com.dokar.anav.javasample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dokar.anav.NavUtils;
import com.dokar.anav.annotation.Navigable;
import com.dokar.anav.ktsample.navigation.NavArgs;
import com.dokar.anav.ktsample.navigation.NavMap;

import kotlin.Unit;

@Navigable
public class MainActivity extends Activity {

    private final int randomInt = (int) (Math.random() * 10000);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.text);
        text.setText("Random id: " + randomInt);

        Button btnGo = findViewById(R.id.button);
        btnGo.setOnClickListener((view) -> {
            go();
        });
    }

    private void go() {
        NavUtils.navigationBuilder(this, NavMap.Article)
                .also(intent -> {
                    // set argument(s)
                    NavArgs.Article.setId(intent, randomInt);
                    return Unit.INSTANCE;
                })
                .onFailure(e -> {
                    Toast.makeText(
                            this,
                            "Cannot open activity: " + e,
                            Toast.LENGTH_SHORT).show();
                    return Unit.INSTANCE;
                })
                .go();
    }
}
