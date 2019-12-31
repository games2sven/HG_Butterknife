package com.highgreat.sven.hg_butterknife;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.highgreat.sven.annotations.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tvText)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HGButterknife.bind(this);
        textView.setText("注入成功");

    }
}
