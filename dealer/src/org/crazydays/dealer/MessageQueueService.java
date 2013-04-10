package org.crazydays.dealer;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MessageQueueService
    extends Service
{
    private final static String MESSAGE_QUEUE_FILE = "messageQueue";
    private final static String MESSAGES_PROPERTY = "messages";

    public interface EventListener
    {
        public void received();
    }

    public class MessageQueueServiceBinder
        extends Binder
    {
        MessageQueueService getService()
        {
            return MessageQueueService.this;
        }
    }

    private MessageQueueServiceBinder binder = new MessageQueueServiceBinder();

    private List<EventListener> listeners = new LinkedList<EventListener>();

    private Queue<JSONObject> queue = new LinkedList<JSONObject>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onCreate()
    {
        Log.i(getClass().getSimpleName(), "onCreate");
        super.onCreate();
        loadMessageQueue();
    }

    @Override
    public void onDestroy()
    {
        Log.i(getClass().getSimpleName(), "onDestroy");
        storeMessageQueue();
        super.onDestroy();
    }

    private void loadMessageQueue()
    {
        Log.i(getClass().getSimpleName(), "loadMessageQueue");
        SharedPreferences preferences =
            getSharedPreferences(MESSAGE_QUEUE_FILE, 0);

        String[] strings =
            preferences.getString(MESSAGES_PROPERTY, "").split(";");

        for (String string : strings) {
            try {
                Log.d(getClass().getSimpleName(), "loading: " + string);
                queue.add(new JSONObject(string));
            } catch (JSONException e) {
                // shouldn't be able to happen unless preferences are corrupted
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MESSAGES_PROPERTY, "");
        editor.commit();
    }

    private void storeMessageQueue()
    {
        Log.i(getClass().getSimpleName(), "storeMessageQueue");

        StringBuilder buffer = new StringBuilder();

        for (JSONObject json : queue) {
            String string = json.toString();

            if (buffer.length() > 0) {
                buffer.append(';');
            }
            buffer.append(string);

            Log.d(getClass().getSimpleName(), "storing: " + string);
        }

        SharedPreferences preferences =
            getSharedPreferences(MESSAGE_QUEUE_FILE, 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(MESSAGES_PROPERTY, buffer.toString());
        editor.commit();
    }

    public void addEventListener(EventListener listener)
    {
        listeners.add(listener);
    }

    public void removeEventListener(EventListener listener)
    {
        listeners.remove(listener);
    }

    public boolean hasMessages()
    {
        return queue.size() > 0;
    }

    public void enqueue(JSONObject message)
    {
        queue.add(message);

        notifyEventListeners();
    }

    private void notifyEventListeners()
    {
        for (EventListener listener : listeners) {
            listener.received();
        }
    }

    public JSONObject dequeue()
    {
        return queue.poll();
    }
}
