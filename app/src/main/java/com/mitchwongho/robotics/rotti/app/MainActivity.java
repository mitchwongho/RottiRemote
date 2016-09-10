package com.mitchwongho.robotics.rotti.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.mitchwongho.robotics.rotti.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.MainThreadSubscription;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 *
 */

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static int REQUEST_ENABLE_BT = 900;
    @Bind(R.id.btn_forward)
    View btnForward;
    @Bind(R.id.btn_backward)
    View btnBackward;
    @Bind(R.id.btn_stop)
    View btnStop;
    @Bind(R.id.btn_left)
    View btnLeft;
    @Bind(R.id.btn_right)
    View btnRight;
    private BluetoothAdapter bluetoothAdapter;
    private UUID uuid;
    private CompositeSubscription subs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        uuid = UUID.randomUUID();
    }

    @Override
    protected void onStart() {
        super.onStart();
        subs = new CompositeSubscription();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //// TODO: 2016/09/09 BT not available
        } else if (!bluetoothAdapter.isEnabled()) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            final Map<String, BluetoothDevice> mapDevices = new HashMap<>(pairedDevices.size());
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
//                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mapDevices.put(device.getName(), device);
                }
                //
                if (mapDevices.containsKey("HC-06")) {
                    final BehaviorSubject<BluetoothSocket> subject = BehaviorSubject.create();
                    final BluetoothDevice device = mapDevices.get("HC-06");
                    getSupportActionBar().setSubtitle(device.getAddress());
                    Log.e(TAG, String.format("Found 'HC-06' : UUID = %s", device.getUuids()[0]));
                    final Subscription subConnection = create(device)
                            .flatMap(bluetoothSocket -> connect(bluetoothAdapter, bluetoothSocket))
                            .filter(BluetoothSocket::isConnected)
                            .subscribe(subject);
                    subs.add(subConnection);

                    final Subscription subFwd = Observable.combineLatest(RxView.clicks(btnForward).map(aVoid -> "F\n".getBytes())
                            , subject.asObservable(), (cmd, bluetoothSocket) -> {
                                if (bluetoothSocket.isConnected()) {
                                    try {
                                        bluetoothSocket.getOutputStream().write(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return bluetoothSocket;
                            })
                            .subscribe();
                    subs.add(subFwd);

                    final Subscription subRev = Observable.combineLatest(RxView.clicks(btnBackward).map(aVoid -> "B\n".getBytes())
                            , subject.asObservable(), (cmd, bluetoothSocket) -> {
                                if (bluetoothSocket.isConnected()) {
                                    try {
                                        bluetoothSocket.getOutputStream().write(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return bluetoothSocket;
                            })
                            .subscribe();
                    subs.add(subRev);

                    final Subscription subStop = Observable.combineLatest(RxView.clicks(btnStop).map(aVoid -> "!\n".getBytes())
                            , subject.asObservable(), (cmd, bluetoothSocket) -> {
                                if (bluetoothSocket.isConnected()) {
                                    try {
                                        bluetoothSocket.getOutputStream().write(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return bluetoothSocket;
                            })
                            .subscribe();
                    subs.add(subStop);

                    final Subscription subLeft = Observable.combineLatest(RxView.clicks(btnLeft).map(aVoid -> "L\n".getBytes())
                            , subject.asObservable(), (cmd, bluetoothSocket) -> {
                                if (bluetoothSocket.isConnected()) {
                                    try {
                                        bluetoothSocket.getOutputStream().write(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return bluetoothSocket;
                            })
                            .subscribe();
                    subs.add(subLeft);

                    final Subscription subRight = Observable.combineLatest(RxView.clicks(btnRight).map(aVoid -> "R\n".getBytes())
                            , subject.asObservable(), (cmd, bluetoothSocket) -> {
                                if (bluetoothSocket.isConnected()) {
                                    try {
                                        bluetoothSocket.getOutputStream().write(cmd);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return bluetoothSocket;
                            })
                            .subscribe();
                    subs.add(subRight);


                }
            }
        }
    }

    private void printError(@NonNull final Throwable t) {
        t.printStackTrace();
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //
            } else {
                //
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        subs.clear();
    }

    private Observable<BluetoothSocket> create(@NonNull final BluetoothDevice device) {
        return Observable.<BluetoothSocket>create(subscriber -> {
            final BluetoothServerSocket[] bss = new BluetoothServerSocket[]{null};
            final BluetoothSocket[] sock = new BluetoothSocket[]{null};
            try {
                sock[0] = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                subscriber.onNext(sock[0]);
            } catch (final Throwable t) {
                t.printStackTrace();
                subscriber.onError(t);
            }

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    //todo
                    try {
                        if (sock[0] != null) {
                            sock[0].close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        sock[0] = null;
                    }
                    try {
                        if (bss[0] != null) {
                            bss[0].close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        bss[0] = null;
                    }
                }
            });
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline());
    }

    private Observable<BluetoothSocket> connect(@NonNull final BluetoothAdapter bluetoothAdapter, @NonNull final BluetoothSocket socket) {
        return Observable.<BluetoothSocket>create(subscriber -> {

            try {
                if (socket.isConnected()) {
                    subscriber.onNext(socket);
                } else {
                    bluetoothAdapter.cancelDiscovery();
                    socket.connect();
                    subscriber.onNext(socket);
                }
            } catch (Throwable t) {
                try {
                    socket.close();
                } catch (IOException e) {
                } finally {
                    t.printStackTrace();
                    subscriber.onError(t);
                }
            }

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    try {
                        if (!socket.isConnected()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline())
                .onErrorResumeNext(throwable -> connect(bluetoothAdapter, socket));
    }

}
