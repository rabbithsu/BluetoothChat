package com.example.android.bluetoothchat;

import android.content.Context;
import android.os.Handler;

import com.example.android.common.logger.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nccu_dct on 15/8/29.
 */
public class XMPPChatService {
    private static final String TAG = "XMPPService";
    public static AbstractXMPPConnection connection;
    private final Handler mHandler;
    private Chat XMPPchat;



    //XMPP
    public static final String HOST = "140.119.164.18";
    public static final int PORT = 5222;
    public static final String SERVICE = "140.119.164.18";
    public static String USERNAME;
    public static String PASSWORD;
    public static MultiUserChat MultiChatroom;
    private ArrayList<String> messages = new ArrayList<String>();



    public XMPPChatService(Context context, Handler handler, String username, String pw) {
        mHandler = handler;
        XMPPchat = null;
        USERNAME = username;
        PASSWORD = pw;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create a connection
                XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
                config.setServiceName(SERVICE);
                config.setHost(HOST);
                config.setPort(PORT);
                //config.setUsernameAndPassword(USERNAME, PASSWORD);
                //config.setDebuggerEnabled(true);
                config.setCompressionEnabled(false);
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);


                //config.setSASLAuthenticationEnabled(false);
                connection = new XMPPTCPConnection(config.build());

                try {
                    connection.connect();
                    Log.d("XMPPChatDemoActivity",
                            "Connected to " + connection.getHost());
                } catch (Exception ex) {
                    BluetoothChatFragment.XMPPing = false;
                    Log.d("XMPPChatDemoActivity", "Failed to connect to "
                            + connection.getHost());
                    Log.d("XMPPChatDemoActivity", ex.toString());
                    //connection = null;
                }
                try {
                    // SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                    connection.login();
                    //connection.login(USERNAME, PASSWORD);
                    Log.d("XMPPChatDemoActivity",
                            "Logged in as " + connection.getUser());

                    // Set the status to available
                    /*Presence presence = new Presence(Presence.Type.available);
                    //Presence.Type.unavailable
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

                    }*/
                    //chat receiver
                    ChatManager chatManager = ChatManager.getInstanceFor(connection);
                    chatManager.addChatListener(
                            new ChatManagerListener() {
                                @Override
                                public void chatCreated(Chat chat, boolean createdLocally) {
                                    if (!createdLocally)
                                        chat.addMessageListener(new ChatMessageListener() {
                                            @Override
                                            public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                                                Log.d("XMPPChatDemoActivity", "Receive: " + message.getBody());
                                                mHandler.obtainMessage(Constants.MESSAGE_XMPP_READ, message.getBody().length(), -1, message.getBody())
                                                        .sendToTarget();
                                            }

                                        });
                                    ;
                                }
                            });
                } catch (Exception ex) {
                    BluetoothChatFragment.XMPPing = false;
                    Log.d("XMPPChatDemoActivity", "Failed to log in as "
                            + USERNAME);
                    Log.d("XMPPChatDemoActivity", ex.toString());
                    connection = null;
                }

                //chatroomtest
                try {
                    if(BluetoothChatFragment.XMPPing){
                        startchat("all@broadcast.140.119.164.18");
                        /*MultiChatroom = joinMultiUserChat("tester1", "", "hi", connection);
                        MultiChatroom.addMessageListener( new  multiListener());*/
                    }
                }catch (Exception e){
                    Log.d("XMPPChatDemoActivity", e.toString());
                }

                //dialog.dismiss();
            }
        });
        t.start();
    }

    public AbstractXMPPConnection getConnection(){
        return this.connection;
    }

    public void write(String out) {
        // Create temporary object

        // Perform the write unsynchronized
        try {
            XMPPchat.sendMessage(out);
            MultiChatroom.sendMessage(out);
            mHandler.obtainMessage(Constants.MESSAGE_XMPP_WRITE, -1, -1, out)
                    .sendToTarget();
        }
        catch (Exception ex){
            Log.d(TAG, "Send message failed.");
        }
    }

    public void relaying(String out) {
        // Create temporary object

        // Perform the write unsynchronized
        try {
            XMPPchat.sendMessage(out);
        }
        catch (Exception ex){
            Log.d(TAG, "Send message failed.");
        }
    }

    public void startchat(String account){
        Log.d(TAG, account);
        new XMPPThread(account);
    }


    private class XMPPThread{
        private final String USRID;


        public XMPPThread(String account) {
            USRID = account;
            Log.d(TAG, "Chat to user: "+USRID);

            Thread m = new Thread(new Runnable() {
                @Override
                public void run(){
                //chat test
                    Chat chat = ChatManager.getInstanceFor(connection).createChat(USRID, new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                            Log.d("XMPPChatDemoActivity", "Receive: " + message.getBody());
                            mHandler.obtainMessage(Constants.MESSAGE_XMPP_READ, message.getBody().length(), -1, message.getBody())
                                .sendToTarget();
                            }

                });
                XMPPchat = chat;

                }
            });
            m.start();

        }

    }

    /**
     * Create a room
     *
     * @Param roomName the name of the room
     */
    public static void createRoom(String roomName) {
        if (connection == null) {
            return;
        }
        try {
            // Create a MultiUserChat
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);

            MultiUserChat muc = manager.getMultiUserChat(roomName
                    + "@conference." + connection.getServiceName());

            //MultiUserChat muc = new MultiUserChat(connection, roomName
            //        + "@conference." + connection.getServiceName());
            // Create a chat room
            muc.create(roomName); // RoomName room name
            // To obtain the chat room configuration form
            Form form = muc.getConfigurationForm();
            // Create a new form to submit the original form according to the.
            Form submitForm = form.createAnswerForm();
            // To submit the form to add a default reply
            List<FormField> fields = form.getFields();
            for (FormField field: fields) {
                //FormField field = (FormField) fields.next();

                if (!FormField.Type.hidden.equals(field.getType())
                        && field.getVariable() != null) {
                    // Set default values for an answer
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            // Set the chat room of the new owner
            List<String> owners = new ArrayList<String>();
            owners.add(connection.getUser());// The user JID
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            // Set the chat room is a long chat room, soon to be preserved
            submitForm.setAnswer("muc#roomconfig_persistentroom", false);
            // Only members of the open room
            submitForm.setAnswer("muc#roomconfig_membersonly", false);
            // Allows the possessor to invite others
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            // Enter the password if needed
            //submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
            // Set to enter the password
            //submitForm.setAnswer("muc#roomconfig_roomsecret", "password");
            // Can be found in possession of real JID role
            // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
            // Login room dialogue
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // Only allow registered nickname log
            submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // Allows the user to modify the nickname
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // Allows the user to register the room
            submitForm.setAnswer("x-muc#roomconfig_registration", false);
            // Send the completed form (the default) to the server to configure the chat room
            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
            // Send the completed form (the default) to the server to configure the chat room
            muc.sendConfigurationForm(submitForm);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * To join the conference room
     *
     * @Param user nickname
     * @Param password meeting room password
     * @Param roomsName meeting room
     * @param connection
     */
    public static MultiUserChat joinMultiUserChat(String user, String password, String roomsName,
                                                  XMPPConnection connection) {
        try {
            // Create a MultiUserChat window using XMPPConnection
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);

            MultiUserChat muc = manager.getMultiUserChat(roomsName
                    + "@conference." + connection.getServiceName());
            //MultiUserChat muc = new MultiUserChat(connection, roomsName
            //        + "@conference." + connection.getServiceName());

            // The number of chat room services will decide to accept the historical record
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            //history.setSince(new Date());
            // Users to join in the chat room
            muc.join(user, password, history, SmackConfiguration.getDefaultPacketReplyTimeout());
            System.out.println("The conference room success....");
            return muc;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("The conference room to fail....");
            return null;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query the conference room member name
     * @param muc
     */
    public static List<String> findMulitUser(MultiUserChat muc){
        List<String> listUser = new ArrayList<String>();
        List<String> it = muc.getOccupants();
        //Traverse the chat room name
        for (String name : it) {
            // Chat room members name
            //String name = StringUtils.parseResource(it.next());
            listUser.add(name);
        }
        return listUser;
    }

    /**
     * Gets the room all conference server
     * @return
     * @throws XMPPException
     */
    /*
    public static List<FriendRooms> getConferenceRoom() throws XMPPException {
        List<FriendRooms> list = new ArrayList<FriendRooms>();
        new ServiceDiscoveryManager(connection);
        if (!MultiUserChat.getHostedRooms(connection,
                connection.getServiceName()).isEmpty()) {

            for (HostedRoom k : MultiUserChat.getHostedRooms(connection,
                    connection.getServiceName())) {

                for (HostedRoom j : MultiUserChat.getHostedRooms(connection,
                        k.getJid())) {
                    RoomInfo info2 = MultiUserChat.getRoomInfo(connection,
                            j.getJid());
                    if (j.getJid().indexOf("@") > 0) {

                        FriendRooms friendrooms = new FriendRooms();
                        friendrooms.setName(j.getName());//The name of the chat room
                        friendrooms.setJid(j.getJid());//JID chat room
                        friendrooms.setOccupants(info2.getOccupantsCount());//The quantity of the chat room in
                        friendrooms.setDescription(info2.getDescription());//Chat room description
                        friendrooms.setSubject(info2.getSubject());//Chat Theme
                        list.add(friendrooms);
                    }
                }
            }
        }
        return list;
    }

    */

    /**
     * The meeting room information monitoring events
     *
     * @author Administrator
     *
     */
    public class multiListener implements MessageListener {

        @Override
        public void processMessage(Message message) {
            //Message message = (Message) packek;
            // Received from the chat room chat message
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Log.d(TAG, message.getBody());
            mHandler.obtainMessage(Constants.MESSAGE_XMPP_READ, message.getBody().length(), -1, message.getBody())
                    .sendToTarget();
            /*
            MucHistory mh = new MucHistory();
            mh.setUserAccount(account);
            String from = StringUtils.parseResource(message.getFrom());
            String fromRoomName = StringUtils.parseName(message.getFrom());
            mh.setMhRoomName(fromRoomName);
            mh.setFriendAccount(from);
            mh.setMhInfo(message.getBody());
            mh.setMhTime(time);
            mh.setMhType("left");*/
        }
    }

    //multiChat.sendMessage(str);//MultiChat chat room object

    /**
     * The conference room status monitoring events
     *
     * @author Administrator
     *
     */
    class ParticipantStatus implements ParticipantStatusListener {

        @Override
        public void adminGranted(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void adminRevoked(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void banned(String arg0, String arg1, String arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void joined(String participant) {
            System.out.println(participant+ " has joined the room.");
        }

        @Override
        public void kicked(String arg0, String arg1, String arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void left(String participant) {
            // TODO Auto-generated method stub
            System.out.println(participant+ " has left the room.");

        }

        @Override
        public void membershipGranted(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void membershipRevoked(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void moderatorGranted(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void moderatorRevoked(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void nicknameChanged(String participant, String newNickname) {
            System.out.println(participant+ " is now known as " + newNickname + ".");
        }

        @Override
        public void ownershipGranted(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void ownershipRevoked(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void voiceGranted(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void voiceRevoked(String arg0) {
            // TODO Auto-generated method stub

        }

    }
}
