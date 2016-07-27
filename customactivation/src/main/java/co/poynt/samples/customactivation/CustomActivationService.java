package co.poynt.samples.customactivation;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;

import co.poynt.api.model.CustomFundingSource;
import co.poynt.api.model.FundingSource;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntActivationService;
import co.poynt.os.services.v1.IPoyntActivationServiceListener;
import timber.log.Timber;

/**
 * This class implements sample custom activation service that shows how to
 * support custom activation during merchant on-boarding.
 */
public class CustomActivationService extends Service {

    public CustomActivationService() {
        Timber.plant(new Timber.DebugTree() {
            // Add the line number to the tag.
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ':' + element.getLineNumber();
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPoyntActivationService.Stub mBinder = new IPoyntActivationService.Stub() {

        @Override
        public void activate(String requestId, IPoyntActivationServiceListener iPoyntActivationServiceListener) throws RemoteException {
            Timber.d("Activation RequestId (%s)", requestId);
            new ActivationTask(requestId, iPoyntActivationServiceListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    /**
     * This task will handle activation request in background.
     */
    public class ActivationTask extends AsyncTask<Void, Void, Void> {

        private IPoyntActivationServiceListener callback;
        private String requestId;

        public ActivationTask(String requestId, IPoyntActivationServiceListener callback) {
            this.callback = callback;
            this.requestId = requestId;
            Timber.d("ActivationTask for requestId (%s)", this.requestId);
        }


        @Override
        protected Void doInBackground(Void... params) {
            // You can return cached business object if you want by calling.
            //   callback.onResponse(business,null,requestId);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            try {
                // Provide an activity intent to be launched to get user input for creating
                // business object.
                Intent intent = new Intent(CustomActivationService.this, ActivationActivity.class);
                // request to launch ActivationActivity activity.
                callback.onLaunchActivity(intent, requestId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}