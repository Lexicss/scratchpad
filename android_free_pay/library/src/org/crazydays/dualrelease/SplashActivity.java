package org.crazydays.dualrelease;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SplashActivity
    extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.splash_activity, menu);
        return true;
    }
}
