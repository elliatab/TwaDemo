package com.firebaseapp.twademo;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.support.customtabs.TrustedWebUtils;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final int SESSION_ID = 96375;

  @Nullable
  private TwaCustomTabsServiceConnection mServiceConnection;

  private String mChromePackage;

  /** We only want to show the update prompt once per instance of this application. */
  private static boolean sChromeVersionChecked;

  @Nullable
  CustomTabsIntent mCustomTabsIntent;

  @Nullable
  private CustomTabsSession mCustomTabsSession;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mCustomTabsIntent != null) {
          TrustedWebUtils.launchAsTrustedWebActivity(
              MainActivity.this,
              mCustomTabsIntent,
              Uri.parse("https://twa-demo.firebaseapp.com/"));
        }
      }
    });


    mChromePackage = CustomTabsClient.getPackageName(this,
        TrustedWebUtils.SUPPORTED_CHROME_PACKAGES, false);

    if (mChromePackage == null) {
      TrustedWebUtils.showNoPackageToast(this);
      finish();
      return;
    }

    if (!sChromeVersionChecked) {
      TrustedWebUtils.promptForChromeUpdateIfNeeded(this, mChromePackage);
      sChromeVersionChecked = true;
    }


    mServiceConnection = new TwaCustomTabsServiceConnection();
    CustomTabsClient.bindCustomTabsService(this, mChromePackage, mServiceConnection);

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  protected CustomTabsSession getSession(CustomTabsClient client) {
    return client.newSession(null, SESSION_ID);
  }

  /**
   * Creates a {@link CustomTabsIntent} to launch the Trusted Web Activity.
   * By default, Trusted Web Activity will be launched in the same Android Task.
   * Override this if you want any special launching behaviour.
   */
  protected CustomTabsIntent getCustomTabsIntent(CustomTabsSession session) {
    return new CustomTabsIntent.Builder(session).build();
  }

  private class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {

    @Override
    public void onCustomTabsServiceConnected(ComponentName componentName,
                                             CustomTabsClient client) {

      if (TrustedWebUtils.warmupIsRequired(MainActivity.this, mChromePackage)) {
        client.warmup(0);
      }

      mCustomTabsSession = getSession(client);
      mCustomTabsIntent  = getCustomTabsIntent(mCustomTabsSession);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.d(TAG, "TwaCustomTabs Service Disconnected");
    }
  }
}
