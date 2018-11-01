package co.poynt.samples.codesamples.Services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.services.v1.IPoyntRatingAndReviewService;
import co.poynt.os.services.v1.IPoyntRatingAndReviewServiceListener;
import co.poynt.os.services.v1.IPoyntSecondScreenRatingEntryListener;
import co.poynt.os.services.v1.IPoyntSecondScreenService;
import co.poynt.samples.codesamples.R;
// Need to be enabled in the manifest to work
public class RatingAndReviewService extends Service {
    public static final String TAG = RatingAndReviewService.class.getSimpleName();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private  IPoyntSecondScreenService secondScreenService;
    private IPoyntRatingAndReviewServiceListener callback;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            secondScreenService = IPoyntSecondScreenService.Stub.asInterface(service);
            Log.d(TAG, "Service connected");
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.poynt_logo);
            try {
                // Scale image is deprecated, just pass null
                secondScreenService.collectRating(1, 5, 1, "How did we do", bitmap, null, ratingEntryListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
        }
    };

    private IPoyntSecondScreenRatingEntryListener ratingEntryListener = new IPoyntSecondScreenRatingEntryListener.Stub() {
        @Override
        public void onRatingEntered(int i) throws RemoteException {
            unbindService(serviceConnection);
            callback.onRatingCollected("Got Rating");
        }

        @Override
        public void onRatingEntryCanceled() throws RemoteException {
            unbindService(serviceConnection);
            callback.onRatingCanceled("Cancelled by user");
        }
    };

    private IPoyntRatingAndReviewService.Stub mBinder = new IPoyntRatingAndReviewService.Stub() {
        @Override
        public void collect(Payment payment, String s, IPoyntRatingAndReviewServiceListener listener) throws RemoteException {
            Log.d(TAG, "Collect rating");
            callback = listener;
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SECOND_SCREEN_SERVICE),
                    serviceConnection, BIND_AUTO_CREATE);
        }

        @Override
        public void cancel(String s) throws RemoteException {
            Log.d(TAG, "Cancelled by user");
        }
    };

}
