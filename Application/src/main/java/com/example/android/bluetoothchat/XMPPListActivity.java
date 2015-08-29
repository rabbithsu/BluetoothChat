package com.example.android.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;

import org.jivesoftware.smack.XMPPConnection;
/**
 * Created by nccu_dct on 15/8/26.
 */
public class XMPPListActivity extends Activity {

    private static final String TAG = "XMPPListActivity";

    private BluetoothAdapter mBtAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.xmpp_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private AdapterView.OnItemClickListener mXMPPClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            //intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
