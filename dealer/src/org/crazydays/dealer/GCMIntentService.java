package org.crazydays.dealer;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import org.crazydays.dealer.MessageQueueService.MessageQueueServiceBinder;

public class GCMIntentService
    extends GCMBaseIntentService
{
    private final static String CONTENT = "content";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            Log.i(GCMIntentService.class.getSimpleName(), "onServiceConnected");
            messageQueueService =
                ((MessageQueueServiceBinder) binder).getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.i(GCMIntentService.class.getSimpleName(),
                "onServiceDisconnected");
            bound = false;
        }
    };

    private Handler handler = new Handler();
    private MessageQueueService messageQueueService;
    private boolean bound;

    public final static void register(Context context)
    {
        GCMRegistrar.checkDevice(context);
        GCMRegistrar.checkManifest(context);

        String registrationId = GCMRegistrar.getRegistrationId(context);
        if (registrationId.equals("")) {
            Log.d(GCMIntentService.class.getSimpleName(), "registering");
            GCMRegistrar.register(context,
                context.getString(R.string.project_id));
        } else {
            Log.d(GCMIntentService.class.getSimpleName(), "registrationId: "
                + registrationId);
        }
    }

    public GCMIntentService()
    {
        super();
        Log.d(getClass().getSimpleName(), "constructor");
    }

    public GCMIntentService(String... senderIds)
    {
        super(senderIds);
        Log.d(getClass().getSimpleName(),
            "constructor: " + senderIds.toString());
    }

    @Override
    public void onCreate()
    {
        Log.i(getClass().getSimpleName(), "onCreate");
        super.onCreate();

        bindMessageQueueService();
    }

    private void bindMessageQueueService()
    {
        bindService(new Intent(this, MessageQueueService.class),
            serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy()
    {
        Log.i(getClass().getSimpleName(), "onDestroy");
        super.onDestroy();
        unbindMessageQueueService();
    }

    private void unbindMessageQueueService()
    {
        unbindService(serviceConnection);
    }

    @Override
    protected void onRegistered(Context context, String content)
    {
        Log.d(getClass().getSimpleName(), "onRegistered: " + content);
    }

    @Override
    protected void onUnregistered(Context context, String content)
    {
        Log.d(getClass().getSimpleName(), "onUnregistered: " + content);
    }

    @Override
    protected void onError(Context context, String content)
    {
        Log.d(getClass().getSimpleName(), "onError: " + content);
    }

    @Override
    protected boolean onRecoverableError(Context context, String content)
    {
        Log.d(getClass().getSimpleName(), "onRecoverableError: " + content);
        return super.onRecoverableError(context, content);
    }

    @Override
    protected void onMessage(final Context context, final Intent intent)
    {
        Log.d(getClass().getSimpleName(), "onMessage");

        if (bound) {
            receivedMessage(intent);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    onMessage(context, intent);
                }
            }, 200);
        }
    }

    private void receivedMessage(Intent intent)
    {
        String content = intent.getStringExtra(CONTENT);
        Log.i(getClass().getSimpleName(), "content: " + content);

        JSONObject json = parse(content);
        messageQueueService.enqueue(json);
    }

    private JSONObject parse(String content)
    {
        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
