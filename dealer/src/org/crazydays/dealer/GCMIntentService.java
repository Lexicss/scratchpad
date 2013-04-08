package org.crazydays.dealer;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
            Log.i("GCMIntentService", "onServiceConnected");
            messageQueueService =
                ((MessageQueueServiceBinder) binder).getService();

            // TODO: implement enqueue local store

            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.i("GCMIntentService", "onServiceDisconnected");
            bound = false;
        }
    };

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
        Log.d(GCMIntentService.class.getSimpleName(), "constructor");
    }

    public GCMIntentService(String... senderIds)
    {
        super(senderIds);
        Log.d(GCMIntentService.class.getSimpleName(), "constructor: "
            + senderIds.toString());
    }

    @Override
    protected void onRegistered(Context context, String content)
    {
        Log.d(GCMIntentService.class.getSimpleName(), "onRegistered: "
            + content);
    }

    @Override
    protected void onUnregistered(Context context, String content)
    {
        Log.d(GCMIntentService.class.getSimpleName(), "onUnregistered: "
            + content);
    }

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        Log.d(GCMIntentService.class.getSimpleName(), "onMessage");

        String content = intent.getStringExtra(CONTENT);
        Log.i(getClass().getSimpleName(), "content: " + content);

        if (bound) {
            JSONObject json = parse(content);
            messageQueueService.enqueue(json);
        } else {
            // TODO: implement local queueing
        }
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

    @Override
    protected void onError(Context context, String content)
    {
        Log.d(GCMIntentService.class.getSimpleName(), "onError: " + content);
    }

    @Override
    protected boolean onRecoverableError(Context context, String content)
    {
        Log.d(GCMIntentService.class.getSimpleName(), "onRecoverableError: "
            + content);
        return super.onRecoverableError(context, content);
    }

    @Override
    public void onCreate()
    {
        Log.i("GCMIntentService", "onCreate");
        super.onCreate();

        bindMessageQueueService();
    }

    @Override
    public void onDestroy()
    {
        Log.i("GCMIntentService", "onDestroy");
        super.onDestroy();
        unbindMessageQueueService();
    }

    private void bindMessageQueueService()
    {
        bindService(new Intent(this, MessageQueueService.class),
            serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMessageQueueService()
    {
        unbindService(serviceConnection);
    }
}
