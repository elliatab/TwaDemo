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

import android.support.customtabs.TrustedWebUtils;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final int SESSION_ID = 96375;

  @Nullable
  private TwaCustomTabsServiceConnection mServiceConnection;

  @Nullable
  CustomTabsIntent mCustomTabsIntent;

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

    String customTabsProviderPackage = CustomTabsClient.getPackageName(this,
        TrustedWebUtils.SUPPORTED_CHROME_PACKAGES, false);

    // TODO: in a production app we would check if a chrome package is available and if it's updated.

    mServiceConnection = new TwaCustomTabsServiceConnection();
    CustomTabsClient.bindCustomTabsService(
        this, customTabsProviderPackage, mServiceConnection);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
  }


  private class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {

    @Override
    public void onCustomTabsServiceConnected(ComponentName componentName,
                                             CustomTabsClient client) {

      // Creates a CustomTabsSession with a constant session id.
      CustomTabsSession session  = client.newSession(null, SESSION_ID);

      // Creates a CustomTabsIntent to launch the Trusted Web Activity.
      mCustomTabsIntent  = new CustomTabsIntent.Builder(session).build();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.d(TAG, "Twa CustomTab Service Disconnected");
    }
  }
}
