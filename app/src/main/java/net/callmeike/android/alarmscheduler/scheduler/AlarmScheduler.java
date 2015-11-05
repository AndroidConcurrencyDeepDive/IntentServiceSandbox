/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.alarmscheduler.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import javax.inject.Inject;

import net.callmeike.android.alarmscheduler.R;
import net.callmeike.android.alarmscheduler.tasks.DaggerTaskComponent;
import net.callmeike.android.alarmscheduler.tasks.SampleTask;

import dagger.Lazy;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class AlarmScheduler extends IntentService {
    private static final String TAG = "SCHEDULER";

    private static final long MS_PER_MINUTE = 60 * 1000;

    private static final String PARAM_TASK = TAG + ".TASK";
    private static final int SAMPLE_TASK = -1;

    public static class AlarmReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            Intent svcIntent = new Intent(ctxt, AlarmScheduler.class);
            svcIntent.putExtras(intent);
            startWakefulService(ctxt, svcIntent);
        }
    }

    public static void startSampleTask(Context ctxt) {
        int interval = ctxt.getResources().getInteger(R.integer.sample_task_interval);
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 100,
                interval * MS_PER_MINUTE,
                getTaskIntent(ctxt, SAMPLE_TASK));
        Log.d(TAG, "sample task started");
    }

    public static void stopSampleTask(Context ctxt) {
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .cancel(getTaskIntent(ctxt, SAMPLE_TASK));
        Log.d(TAG, "sample task stopped");
    }

    private static PendingIntent getTaskIntent(Context ctxt, int taskId) {
        Intent intent = new Intent(ctxt, AlarmReceiver.class);
        intent.putExtra(PARAM_TASK, taskId);
        return PendingIntent.getBroadcast(
            ctxt,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Inject volatile Lazy<SampleTask> sampleTask;

    public AlarmScheduler() { super(TAG); }

    @Override
    @UiThread
    public void onCreate() {
        super.onCreate();
        DaggerTaskComponent.create().inject(this);
    }

    @Override
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        try {
            int op = intent.getIntExtra(PARAM_TASK, 0);
            switch (op) {
                case SAMPLE_TASK:
                    sampleTask.get().run();
                    break;
                default:
                    Log.w(TAG, "unexpected op: " + op);
            }
        }
        finally {
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }
}
