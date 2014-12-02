package com.codemany.chinesecard;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = getTabHost();

        TabSpec tabQuery = tabHost.newTabSpec("tab_query");
        tabQuery.setIndicator(getString(R.string.tab_query));
        tabQuery.setContent(new Intent(this, QueryActivity.class));

        TabSpec tabGenerate = tabHost.newTabSpec("tab_generate");
        tabGenerate.setIndicator(getString(R.string.tab_generate));
        tabGenerate.setContent(new Intent(this, GenerateActivity.class));

        tabHost.addTab(tabQuery);
        tabHost.addTab(tabGenerate);
        tabHost.setCurrentTab(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
