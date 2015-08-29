/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.*;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.*;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterEntries;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.util.StringUtils;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;




import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {
    //2 steps
    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private MessageAdapter mConversationArrayAdapter;
    private List<CheckMessage> mdata;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    //auto
    ArrayList<BluetoothDevice> device = new ArrayList<BluetoothDevice>();

    //XMPP
    //private XMPPConnection connection;

    public static final String HOST = "45.55.60.199";
    public static final int PORT = 5222;
    public static final String SERVICE = "45.55.60.199";
    public static final String USERNAME = "rabbithsu";
    public static final String PASSWORD = "123456";
    private ArrayList<String> messages = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        XMPPconnect();
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            while (!mBluetoothAdapter.isEnabled()) {

            }
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
            mChatService = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        //getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            //Toast.makeText(getActivity(), mChatService.getState()+"", Toast.LENGTH_LONG).show();
            /*if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                //mChatService.start();
                mChatService = null;
                setupChat();
            }*/
            mChatService = null;
            setupChat();

        }
        else {
            setupChat();
        }


    }
    @Override
    public void onStop() {
        super.onStop();
        device.clear();
        if(mChatService != null) {
            mChatService.stop();
            mChatService=null;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e){

        }
        if(mChatService != null) {
            mChatService.stop();
            mChatService=null;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //mConversationView = (ListView) view.findViewById(R.id.chat);
        mConversationView =(ListView) view.findViewById(R.id.chat);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);

    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mdata = LoadData();

        mConversationArrayAdapter = new MessageAdapter(getActivity(), mdata);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //try auto
        //Toast.makeText(getActivity(), "Start try.", Toast.LENGTH_SHORT).show();
        //doDiscovery();


    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
            //startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            mdata.clear();
                            mConversationArrayAdapter.Refresh();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mdata.add(new CheckMessage(CheckMessage.MessageType_From, "Me:  " + writeMessage));
                    mConversationArrayAdapter.Refresh();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mdata.add(new CheckMessage(CheckMessage.MessageType_To,mConnectedDeviceName + ":  " + readMessage));
                    mConversationArrayAdapter.Refresh();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }
    private List<CheckMessage> LoadData(){
        List<CheckMessage> Messages=new ArrayList<CheckMessage>();
        return Messages;
    }

    //auto receiver
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(getActivity(), "Receive.", Toast.LENGTH_SHORT).show();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(getActivity(), "Add.", Toast.LENGTH_SHORT).show();
                BluetoothDevice aaa = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.add(aaa);
                Toast.makeText(getActivity(), "Added.", Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getActivity(), "Finish.", Toast.LENGTH_SHORT).show();
                getActivity().unregisterReceiver(receiver);
                if(!device.isEmpty()){
                    waterchat();
                }

            }

        }
    };
    private synchronized void waterchat() {
        boolean cflag = false;
        Toast.makeText(getActivity(), "In auto.", Toast.LENGTH_SHORT).show();
        if (device.isEmpty()){
            Toast.makeText(getActivity(), "Empty.", Toast.LENGTH_SHORT).show();
        }
        else {
            for(int count = 0; count < device.size(); count++) {
                Toast.makeText(getActivity(), "Connecting.", Toast.LENGTH_SHORT).show();
                cflag = mChatService.Autoconnect(device.get(count), false);
                if(cflag) {
                    Toast.makeText(getActivity(), "Success.", Toast.LENGTH_SHORT).show();
                    device.clear();
                    break;
                }
                //mChatService.failed();
            }
            Toast.makeText(getActivity(), "Fail.", Toast.LENGTH_SHORT).show();
            if(!cflag){
                Toast.makeText(getActivity(), "Set fails.", Toast.LENGTH_SHORT).show();
                device.clear();
                mChatService.failed();
            }

            /*while (!device.isEmpty()) {

                if (mChatService.Autoconnect(device.remove(0), false)){
                    //Toast.makeText(getActivity(), "break.", Toast.LENGTH_SHORT).show();
                    device.clear();
                    break;
                }
                else {
                    //Toast.makeText(getActivity(), "Fail.", Toast.LENGTH_SHORT).show();
                    if (device.isEmpty()) {
                        mChatService.failed();
                        //Toast.makeText(getActivity(), "Listen.", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            //device.clear();
            lock = false;*/
        }
    }
    private void doDiscovery(){
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        mBluetoothAdapter.startDiscovery();
    }

    //XMPP
    public void XMPPconnect(){
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create a connection
                XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
                config.setServiceName("45.55.60.199");
                config.setHost("45.55.60.199");
                config.setPort(5222);
                config.setUsernameAndPassword("rabbithsu", "123456");
                //config.setDebuggerEnabled(true);
                config.setCompressionEnabled(false);
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);



                //config.setSASLAuthenticationEnabled(false);
                AbstractXMPPConnection connection = new XMPPTCPConnection(config.build());

                try {
                    connection.connect();
                    Log.d("XMPPChatDemoActivity",
                            "Connected to " + connection.getHost());
                } catch (Exception ex) {
                    Log.d("XMPPChatDemoActivity", "Failed to connect to "
                            + connection.getHost());
                    Log.d("XMPPChatDemoActivity", ex.toString());
                    //connection = null;
                }
                try {
                    // SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                    connection.login(USERNAME, PASSWORD);
                    Log.d("XMPPChatDemoActivity",
                            "Logged in as " + connection.getUser());
                    //chat test
                    Chat chat = ChatManager.getInstanceFor(connection) .createChat("rabbithsu@45.55.60.199", new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                            Log.d("XMPPChatDemoActivity", "Receive: "+ message.getBody());
                        }


                    });
                    chat.sendMessage("Howdy!");
                    // Set the status to available
                    Presence presence = new Presence(Presence.Type.available);
                    connection.sendStanza(presence);
                    //setReceive(connection);

                    Roster roster = Roster.getInstanceFor(connection);
                    if (!roster.isLoaded())
                        roster.reloadAndWait();
                    Collection<RosterEntry> entries = roster.getEntries();

                    for (RosterEntry entry : entries) {
                        //System.out.println(entry);
                        Log.d("XMPPChatDemoActivity", "USER:  "
                                + entry.getUser());
                        //Toast.makeText(getActivity(), entry.getName(), Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception ex) {
                    Log.d("XMPPChatDemoActivity", "Failed to log in as "
                            + USERNAME);
                    Log.d("XMPPChatDemoActivity", ex.toString());
                    connection = null;
                }

                //dialog.dismiss();
            }
        });
        t.start();
    }

/*
    public void setReceive(AbstractXMPPConnection connect) {
        //connection = connection;
        Chat chat = ChatManager.getInstanceFor(connect).createChat("jsmith@jivesoftware.com", new MessageListener() {
            @Override
            public void processMessage(Message message) {
                System.out.println("Received message: " + message);
            }
        });
    }*/
}
