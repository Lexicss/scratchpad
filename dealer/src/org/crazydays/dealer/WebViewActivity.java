package org.crazydays.dealer;


import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

import org.crazydays.dealer.MessageQueueService.MessageQueueServiceBinder;

public class WebViewActivity
    extends Activity
    implements MessageQueueService.EventListener
{
    private WebView webView;
    private Handler handler = new Handler();
    private MessageQueueService messageQueueService;
    private boolean bound;
    private boolean initialized;

    class JavascriptHelper
    {
        @JavascriptInterface
        public void initialized()
        {
            Log.i(WebViewActivity.class.getSimpleName(), "initialized");
            initialized = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        setupWebView();
        registerPushNotifications();
        bindMessageQueueService();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView()
    {
        webView = (WebView) findViewById(R.id.web_view);

        // enable javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // add javascript helper
        webView.addJavascriptInterface(new JavascriptHelper(),
            "JavascriptHelper");

        // load page
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void registerPushNotifications()
    {
        GCMIntentService.register(this);
    }

    private void bindMessageQueueService()
    {
        bindService(new Intent(this, MessageQueueService.class),
            serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy()
    {
        unbindMessageQueueService();
        super.onDestroy();
    }

    private void unbindMessageQueueService()
    {
        unbindService(serviceConnection);
    }

    @Override
    public void received()
    {
        if (isReady()) {
            processMessages();
        } else {
            Log.d(getClass().getSimpleName(), "delaying message processing");
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    received();
                }
            }, 10000);
        }
    }

    private boolean isReady()
    {
        return bound && initialized;
    }

    private void processMessages()
    {
        while (messageQueueService.hasMessages()) {
            JSONObject object = messageQueueService.dequeue();
            if (object == null) {
                Log.w(getClass().getSimpleName(), "missing object");
            } else {
                Log.i(getClass().getSimpleName(),
                    "message: " + object.toString());
                injectMessage(object.toString());
            }
        }
    }

    private void injectMessage(final String content)
    {
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                webView.loadUrl("javascript:Pusher.receive(" + content + ");");
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            Log.i(WebViewActivity.class.getSimpleName(), "onServiceConnected");
            messageQueueService =
                ((MessageQueueServiceBinder) binder).getService();
            messageQueueService.addEventListener(WebViewActivity.this);
            bound = true;
            if (messageQueueService.hasMessages()) {
                received();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.i(WebViewActivity.class.getSimpleName(),
                "onServiceDisconnected");
            messageQueueService.removeEventListener(WebViewActivity.this);
            bound = false;
        }
    };
}
