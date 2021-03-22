package com.dokar.anav.javasample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.dokar.anav.annotation.Navigable;
import com.dokar.anav.ktsample.navigation.NavArgs;

@Navigable(args = "id", argTypes = {int.class})
public class ArticleActivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        setContentView(textView);

        // get argument(s)
        int id = NavArgs.Article.getId(getIntent());

        textView.setText("id: " + id);
    }
}
