package com.prime.acsclient.communicate;

import static android.os.Environment.DIRECTORY_DCIM;

import android.app.AlarmManager;
import android.bluetooth.BluetoothClass;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.acsdata.ParseACSData;
import com.prime.acsclient.common.BroadcastDefine;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.acsdata.ProcessACSData;
import com.prime.acsclient.common.DeviceControlUnit;
import com.prime.acsclient.common.MqttCommand;
import com.prime.acsclient.prodiver.ACSDataContentProvider;
import com.prime.acsclient.prodiver.ACSDatabase;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MqttHandler extends Handler {
    private final static String TAG = "ACS_MqttHandler" ;
    private static HandlerThread g_mqtt_HandlerThread = null;
    private MqttManager g_mqtt_manager = null ;
    private int g_instant_monitoring_period = 0;

    public MqttHandler(Looper looper) {
        super(looper);
    }

    public static MqttHandler createHandler() {
        Log.d( TAG, "createHandler") ;
        g_mqtt_HandlerThread = new HandlerThread("mqtt_HandlerThread");
        g_mqtt_HandlerThread.start();
        return new MqttHandler(g_mqtt_HandlerThread.getLooper());
    }

    @Override
    public void handleMessage(Message message) {
        int caseid = message.what ;
        switch ( caseid )
        {
            case CommonDefine.ACS_MQTT_SERVER_CONNECT_CASEID:
            {
                Log.d( TAG, "connect to mqtt!!!" ) ;
                mqtt_server_connect() ;
            }break;
            case CommonDefine.ACS_MQTT_SERVER_DISCONNECT_CASEID:
            {
                Log.d( TAG, "disconnect to mqtt!!!" ) ;
                if ( g_mqtt_manager != null )
                {
                    g_mqtt_manager.disconnect();
                    g_mqtt_manager = null;
                }
            }break;
            case CommonDefine.ACS_MQTT_SERVER_PUBLISH_CASEID:
            {
                if ( g_mqtt_manager != null )
                {
                    JSONObject publish_message = (JSONObject) message.obj;
                    int arg1 = message.arg1;
                    g_mqtt_manager.publishMessage(publish_message.toString(), arg1);
                }

            }break;
            case CommonDefine.ACS_MQTT_RESUBSCRIBE_CASEID:
            {
                if ( g_mqtt_manager != null )
                {
                    int[] type_list = (int[]) message.obj;
                    for( int type : type_list )
                    {
                        g_mqtt_manager.unsubscribeTopic(type);
                        g_mqtt_manager.mqtt_topic_info_update(type);
                        g_mqtt_manager.subscribeToTopic(type);
                    }
                }
            }break;
            case CommonDefine.ACS_MQTT_INNER_TRIGGER_BOX_STATUS_CASEID:
            {
                ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_BOX_STATUS);
            }break;
            case CommonDefine.ACS_MQTT_EVERY_DAY_CHECKING_CASEID:
            {
                ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_BOX_STATUS);
                sendEmptyMessageDelayed(CommonDefine.ACS_MQTT_EVERY_DAY_CHECKING_CASEID, AlarmManager.INTERVAL_DAY);
            }break;
            case CommonDefine.ACS_MQTT_INSTANT_MONITORING_CHECKING_CASEID:
            {
                if ( g_instant_monitoring_period > 0 )
                {
                    DeviceControlUnit.prepare_instant_monitoring_data() ;
                    ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_INSTANT_MONITORING);
                    sendEmptyMessageDelayed(CommonDefine.ACS_MQTT_INSTANT_MONITORING_CHECKING_CASEID, g_instant_monitoring_period*CommonDefine.ONE_SECOND);
                }
            }break;
            case CommonDefine.ACS_MQTT_DEVELOP_TEST_CASEID:
            {
                String objStr = (String) message.obj;
                JSONObject mqtt_struct_json = null;
                try {
                    mqtt_struct_json = new JSONObject(objStr);
                    process_mqtt_command(mqtt_struct_json);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }break;
        }
    }

    public void mqtt_server_connect()
    {
        if ( g_mqtt_manager != null )
        {
            g_mqtt_manager.disconnect();
            g_mqtt_manager = null ;
        }
        g_mqtt_manager = new MqttManager(ACSService.g_context);
        g_mqtt_manager.connect();
    }

    public static class MQTT_TOPIC_INFO
    {
        int mqtt_type ;
        String[] subscribe_topic = null ;
        int qos = 0 ;
        public MQTT_TOPIC_INFO( int type, String[] topic, int qos_in )
        {
            mqtt_type = type ;
            subscribe_topic = topic ;
            qos = qos_in ;
        }
    }
    public class MqttManager {

        private static final String TAG = "ACS_MqttManager";
        public static final int MQTT_TOPIC_TYPE_SINGLE_DEVICE = 0 ;
        public static final int MQTT_TOPIC_TYPE_ALL_DEVICE = 1 ;
        public static final int MQTT_TOPIC_TYPE_TBC_DEVICE = 2 ;
        public static final int MQTT_TOPIC_TYPE_OTA_GROUP = 3 ;
        public static final int MQTT_TOPIC_TYPE_OTA_GROUP_FWVER = 4 ;
        public static final int MQTT_TOPIC_TYPE_APP_GROUP = 5 ;
        public static final int MQTT_TOPIC_TYPE_APP_GROUP_FWVER = 6 ;
        public static final int MQTT_TOPIC_TYPE_TVMAIL = 7 ;
        public static final int MQTT_TOPIC_TYPE_INFO = 8 ;
        public static final int MQTT_TOPIC_TYPE_TEST = 9 ;
        private MqttAndroidClient g_mqtt_android_client = null ;
        //        private final String g_ssl_server_uri = "ssl://" + HttpContentValue.ProvisionResponse.mqtt_server_ip + ":" + HttpContentValue.ProvisionResponse.mqtt_tls_server_port ;
        private final String g_tcp_server_uri = "tcp://" + HttpContentValue.ProvisionResponse.mqtt_server_ip + ":" + HttpContentValue.ProvisionResponse.mqtt_server_port ;
//        private final String g_tcp_server_uri = "tcp://10.1.4.213:1883" ;
        private final String g_client_id = HttpContentValue.ProvisionResponse.mqtt_client_id ;
//        private final String g_client_id = HttpContentValue.ProvisionDeviceInfo.eth_mac_address ;
        private ArrayList<MQTT_TOPIC_INFO> g_mqtt_topic_info = null;
        private boolean is_subscribe_finished = false ;
        public void mqtt_topic_info_init()
        {
            g_mqtt_topic_info = new ArrayList<>();
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_SINGLE_DEVICE, new String[]{HttpContentValue.ProvisionResponse.mqtt_subscribe_topics}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_ALL_DEVICE, new String[]{"/home/all"}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_TBC_DEVICE, new String[]{"/home/" + HttpContentValue.ProvisionResponse.project_name}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_OTA_GROUP, new String[]{"/home/ota/" + HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_OTA_GROUP_FWVER, new String[]{"/home/ota/" + HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name + "/" + HttpContentValue.ProvisionDeviceInfo.fw_version}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_APP_GROUP, new String[]{"/home/app/" + HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_APP_GROUP_FWVER, new String[]{"/home/app/" + HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name + "/" + HttpContentValue.ProvisionDeviceInfo.fw_version}, HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_TVMAIL, CommonFunctionUnit.jsonArray_To_StringArray(HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic), HttpContentValue.ProvisionResponse.mqtt_qos)) ;
            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_INFO, CommonFunctionUnit.jsonArray_To_StringArray(HttpContentValue.ProvisionResponse.mqtt_topic_info), HttpContentValue.ProvisionResponse.mqtt_qos)) ;
//            g_mqtt_topic_info.add(new MQTT_TOPIC_INFO(MQTT_TOPIC_TYPE_TEST, new String[]{"/JIMTEST/AAABBBCCC/"}, 2)) ;
        }

        public void mqtt_topic_info_update( int type )
        {
            for (int specific_type_index = 0; specific_type_index < g_mqtt_topic_info.size(); specific_type_index++)
            {
                if (g_mqtt_topic_info.get(specific_type_index).mqtt_type == type)
                {
                    switch (specific_type_index) {
                        case MQTT_TOPIC_TYPE_SINGLE_DEVICE: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{HttpContentValue.ProvisionResponse.mqtt_subscribe_topics}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_ALL_DEVICE: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/all"}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_TBC_DEVICE: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/" + HttpContentValue.ProvisionResponse.project_name}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_OTA_GROUP: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/ota/" + HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_OTA_GROUP_FWVER: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/ota/" + HttpContentValue.ProvisionResponse.mqtt_ota_topic_group_name + "/" + HttpContentValue.ProvisionDeviceInfo.fw_version}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_APP_GROUP: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/app/" + HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_APP_GROUP_FWVER: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, new String[]{"/home/app/" + HttpContentValue.ProvisionResponse.mqtt_app_topic_group_name + "/" + HttpContentValue.ProvisionDeviceInfo.fw_version}, HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_TVMAIL: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, CommonFunctionUnit.jsonArray_To_StringArray(HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic), HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                        case MQTT_TOPIC_TYPE_INFO: {
                            g_mqtt_topic_info.set(specific_type_index, new MQTT_TOPIC_INFO(type, CommonFunctionUnit.jsonArray_To_StringArray(HttpContentValue.ProvisionResponse.mqtt_topic_info), HttpContentValue.ProvisionResponse.mqtt_qos));
                        }break;
                    }
                    break;
                }
            }
        }
        private final String publishTopic = HttpContentValue.ProvisionResponse.mqtt_publish_topics ;
        private IMqttActionListener g_mqtt_action_listener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                if ( asyncActionToken != null && asyncActionToken.getTopics() != null ) {
                    for (String subTopic : asyncActionToken.getTopics()) {
                        Log.d(TAG, "mqtt_action_listener onSuccess topic : " + subTopic);
                    }

                    is_subscribe_finished = true ;
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                if ( exception != null )
//                    exception.printStackTrace();
                if ( asyncActionToken != null && asyncActionToken.getTopics() != null ) {
                    for (String subTopic : asyncActionToken.getTopics()) {
                        Log.d(TAG, "mqtt_action_listener onFailure topic : " + subTopic);
                    }
                }
            }
        };

        private IMqttMessageListener g_mqtt_message_listener = new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d(TAG, "mqtt receive : " + topic + " message : " + message);
                /*
                message format
                {
                    "task_id":2509177,
                    "command":"reboot",
                    "args":
                }
                 */
                JSONObject mqtt_struct_json = new JSONObject(new String(message.getPayload()));
                process_mqtt_command(mqtt_struct_json);
            }
        };
        public MqttManager(Context context) {
            mqtt_topic_info_init() ;
            g_mqtt_android_client = new MqttAndroidClient(context, g_tcp_server_uri, g_client_id);
            g_mqtt_android_client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    if ( cause != null )
                        Log.d( TAG, CommonFunctionUnit.getMethodName() + " Connection lost: " + cause.getMessage());
                    else
                        Log.d( TAG, CommonFunctionUnit.getMethodName() + " Connection lost" ) ;
                    DeviceControlUnit.update_connect_acs_server_time(false);
//                    connect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, CommonFunctionUnit.getMethodName() + " Message arrived: " + topic + ": " + new String(message.getPayload()));
                    JSONObject mqtt_struct_json = new JSONObject(new String(message.getPayload()));
                    process_mqtt_command(mqtt_struct_json);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        Log.d( TAG, CommonFunctionUnit.getMethodName() + " token:" + token.getMessage().toString() ) ;
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.d( TAG, CommonFunctionUnit.getMethodName() + " reconnect:" + reconnect + " serverURI:" + serverURI ) ;
                    if ( reconnect )
                        unsubscribeTopic();
                    subscribeToTopic();
                    ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_BOX_STATUS);
                    DeviceControlUnit.update_connect_acs_server_time(true);
                }
            });
        }

        public void connect() {
            Log.d(TAG, "try to connect mqtt server : " + g_tcp_server_uri) ;
            try {
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setAutomaticReconnect(true);
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setUserName(HttpContentValue.ProvisionResponse.mqtt_user_name);
                g_mqtt_android_client.connect(mqttConnectOptions,null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Connected to: " + asyncActionToken.getClient().getServerURI());
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        g_mqtt_android_client.setBufferOpts(disconnectedBufferOptions);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "Failed to connect: " + asyncActionToken.getClient().getServerURI() + ": " + exception.toString());
                        if(!g_mqtt_android_client.isConnected()) {
                            g_mqtt_android_client.unregisterResources();
                            g_mqtt_android_client = null ;
                            ACSService.notify_acsservice(CommonDefine.ACS_MQTT_SERVER_CONNECT_CASEID, null, 0, 0);
                        }
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        public void subscribeToTopic()
        {
            try {
                for ( MQTT_TOPIC_INFO mqtt_topic_info : g_mqtt_topic_info )
                {
                    if ( mqtt_topic_info.subscribe_topic == null )
                        continue;

                    Log.d( TAG, "subscribe topic type : " + mqtt_topic_info.mqtt_type ) ;
                    for (String topic_str : mqtt_topic_info.subscribe_topic)
                        Log.d(TAG, "subscribe topic name : " + topic_str);
                    if ( mqtt_topic_info.subscribe_topic.length > 0 ) {
                        int[] qos_array = new int[mqtt_topic_info.subscribe_topic.length];
                        Arrays.fill(qos_array, mqtt_topic_info.qos);
                        IMqttMessageListener[] mqtt_message_listener_array = new IMqttMessageListener[mqtt_topic_info.subscribe_topic.length];
                        Arrays.fill(mqtt_message_listener_array, g_mqtt_message_listener);
                        g_mqtt_android_client.subscribe(mqtt_topic_info.subscribe_topic, qos_array, null, g_mqtt_action_listener);
                        g_mqtt_android_client.subscribe(mqtt_topic_info.subscribe_topic, qos_array, mqtt_message_listener_array);
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        public void subscribeToTopic( int type )
        {
            try {
                Log.d(TAG, "subscribeToTopic type : " + type + " g_mqtt_topic_info.size() " + g_mqtt_topic_info.size()) ;
                for (int specific_type_index = 0; specific_type_index < g_mqtt_topic_info.size(); specific_type_index++)
                {
                    Log.d(TAG, "subscribe topic type : " + g_mqtt_topic_info.get(specific_type_index).mqtt_type + " type " + type ) ;
                    if ( g_mqtt_topic_info.get(specific_type_index).mqtt_type == type && g_mqtt_topic_info.get(specific_type_index).subscribe_topic != null )
                    {
                        for (String topic_str : g_mqtt_topic_info.get(specific_type_index).subscribe_topic)
                            Log.d(TAG, "subscribe topic name : " + topic_str);
                        Log.d(TAG, "g_mqtt_topic_info.get(specific_type_index).qos = " + g_mqtt_topic_info.get(specific_type_index).qos ) ;
                        if ( g_mqtt_topic_info.get(specific_type_index).subscribe_topic.length > 0 ) {
                            int[] qos_array = new int[g_mqtt_topic_info.get(specific_type_index).subscribe_topic.length];
                            Arrays.fill(qos_array, g_mqtt_topic_info.get(specific_type_index).qos);
                            IMqttMessageListener[] mqtt_message_listener_array = new IMqttMessageListener[g_mqtt_topic_info.get(specific_type_index).subscribe_topic.length];
                            Arrays.fill(mqtt_message_listener_array, g_mqtt_message_listener);
                            g_mqtt_android_client.subscribe(g_mqtt_topic_info.get(specific_type_index).subscribe_topic, qos_array, null, g_mqtt_action_listener);
                            g_mqtt_android_client.subscribe(g_mqtt_topic_info.get(specific_type_index).subscribe_topic, qos_array, mqtt_message_listener_array);
                            break;
                        }
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        public void unsubscribeTopic()
        {
			if(g_mqtt_android_client == null)
				return;
            try {
                for ( MQTT_TOPIC_INFO mqtt_topic_info : g_mqtt_topic_info ) {
                    if ( mqtt_topic_info.subscribe_topic == null )
                        continue;

                    g_mqtt_android_client.unsubscribe(mqtt_topic_info.subscribe_topic, null, g_mqtt_action_listener);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        public void unsubscribeTopic( int type )
        {
			if(g_mqtt_android_client == null)
				return;			
            try {
                for (int specific_type_index = 0; specific_type_index < g_mqtt_topic_info.size(); specific_type_index++)
                {
                    if ( g_mqtt_topic_info.get(specific_type_index).mqtt_type == type && g_mqtt_topic_info.get(specific_type_index).subscribe_topic != null )
                    {
                        g_mqtt_android_client.unsubscribe(g_mqtt_topic_info.get(specific_type_index).subscribe_topic, null, g_mqtt_action_listener);
                        break;
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        public void publishMessage(String message, int arg1) {
            try {
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(message.getBytes());
                mqttMessage.setQos(2);
                if ( publishTopic != null && is_subscribe_finished )
                {
                    g_mqtt_android_client.publish(publishTopic, mqttMessage);
                    if ( arg1 == ProcessACSData.MQTT_PUSLISH_BOX_STATUS )
                    {
                        CommonFunctionUnit.del_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.SetupWizard.AGREE_EULA_CONTENT) ;
                    }
                }
                Log.d(TAG, "Message published : " + message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        public void disconnect() {
            try {
                unsubscribeTopic();
				if(g_mqtt_android_client != null)
                	g_mqtt_android_client.disconnect();
                Log.d(TAG, "Disconnected");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void check_common_device_info( JSONObject jsonObject )
    {
        if ( jsonObject.has(ACSServerJsonFenceName.ProvisionResponse.DEVICE_PARAMS) ||
                jsonObject.has(ACSServerJsonFenceName.ProvisionResponse.DEVICE_SETTINGS) )
        {
            ParseACSData.parse_common_device_info(jsonObject);
            ProcessACSData.process_all_data(ProcessACSData.MQTT_DATA_COMMON_DEVICE_INFO);
        }
    }

    private void process_mqtt_command( JSONObject mqtt_struct_json )
    {
        if ( mqtt_struct_json == null )
        {
            Log.d(TAG, "message payload is null !!!!");
            return;
        }

		/* debug, save acs command to db */
        if (SystemProperties.getInt(CommonDefine.ACS_MQTT_DEBUG_FLAG, 0) == 1)
        {
            Log.d(TAG, "save mqtt_struct_json to db");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date Time = new Date();
            String str = format.format(Time.getTime());
            Log.d(TAG, "process_mqtt_command = "+mqtt_struct_json.toString());
            CommonFunctionUnit.update_data_without_notify_launcher(str,mqtt_struct_json.toString());
        }

        int task_id = CommonFunctionUnit.get_json_int(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.TASK_ID, CommonDefine.DEFAULT_INT_VALUE);
        String mqtt_command = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.COMMAND, MqttCommand.UNKNOW_COMMAND);
        Log.d( TAG, "receive mqtt command = " + mqtt_command ) ;

        check_common_device_info(mqtt_struct_json);
        switch ( mqtt_command )
        {
            case MqttCommand.REBOOT:
            {
                DeviceControlUnit.reboot_device();
            }break;
            case MqttCommand.GET_INFO:
            {
                String upload_url = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null);
                ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_BOX_STATUS);

                if ( HttpContentValue.ProvisionResponse.is_save_log_enable )
                { // "D4C8B0BAC76B-dump-1727249192039.log"
                    HttpContentValue.ProvisionResponse.log_upload_url = upload_url;
                    ACSService.notify_acsservice(CommonDefine.ACS_LOGCAT_UPLOAD_CASEID, null, 0, 0);
                }

                { //  "D4C8B0BAC76B-info-1728890524348.log"
                    String file_path = ACSService.g_context.getApplicationInfo().dataDir + CommonDefine.GET_INFO_BOX_INFO;
                    DeviceControlUnit.prepare_box_info(ACSService.g_context, file_path);
                    DeviceControlUnit.upload_file_to_acs_server( upload_url + "?mac=" + HttpContentValue.ProvisionDeviceInfo.eth_mac_address, file_path );
                }

                { // "D4C8B0BAC76B-tombstones-1728890347840.log"
                    String file_path = ACSService.g_context.getApplicationInfo().dataDir + CommonDefine.GET_INFO_TOMBSTONES;
                    DeviceControlUnit.prepare_tombstones(file_path);
                    DeviceControlUnit.upload_file_to_acs_server( upload_url + "?mac=" + HttpContentValue.ProvisionDeviceInfo.eth_mac_address + "&type=tombstones", file_path );
                }

                { // "D4C8B0BAC76B-traces-1728890373324.log"
                    String file_path = ACSService.g_context.getApplicationInfo().dataDir + CommonDefine.GET_INFO_ANR;
                    DeviceControlUnit.prepare_anr(file_path);
                    DeviceControlUnit.upload_file_to_acs_server( upload_url + "?mac=" + HttpContentValue.ProvisionDeviceInfo.eth_mac_address + "&type=traces", file_path );
                }
            }break;
            case MqttCommand.SCREEN_CAPTURE:
            {
                String upload_url = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null);
                Log.d( TAG, "screen_capture upload_url : " + upload_url ) ;
                String file_path = ACSService.g_context.getApplicationInfo().dataDir + CommonDefine.SCREEN_CAPTURE_FILE_NAME ;
                DeviceControlUnit.screencapture(file_path);
                DeviceControlUnit.upload_file_to_acs_server( upload_url + "?mac=" + HttpContentValue.ProvisionDeviceInfo.eth_mac_address, file_path );
            }break;
            case MqttCommand.BT_UNPAIR:
            {
                DeviceControlUnit.unpair_bt_device(ACSService.g_context);
            }break;
            case MqttCommand.SET_PARENTAL_LOCK:
            {
                String arg = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null) ;
                if ( arg != null && arg.length() > 0 )
                    DeviceControlUnit.set_parental_lock_password(arg);
            }break;
            case MqttCommand.DO_FACTORY_RESET:
            {
                DeviceControlUnit.do_factory_reset() ;
            }break;
            case MqttCommand.GET_HDMI_INFO:
            {
                DeviceControlUnit.prepare_HDMI_info_data() ;
                ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_HDMI_INFO);
            }break;
            case MqttCommand.INSTANT_MONITORING:
            {
                g_instant_monitoring_period = CommonFunctionUnit.get_json_int(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, 0) ;
                Log.d( TAG, "instant_monitoring_period : " + g_instant_monitoring_period ) ;
                ACSService.notify_acsservice(CommonDefine.ACS_MQTT_INSTANT_MONITORING_CHECKING_CASEID, null, 0, 0);
            }break;
            case MqttCommand.FORCE_TUNE:
            {
                String service_id = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null) ;
                if ( service_id != null )
                    DeviceControlUnit.force_tune( Integer.parseInt(service_id) );
            }break;
            case MqttCommand.CHANNEL_SCAN:
            {
                try {
                    JSONObject tune_param = new JSONObject(CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null));
                    if ( tune_param != null ) {
                        int symbol_rate = CommonFunctionUnit.get_json_int(tune_param, ACSServerJsonFenceName.MqttCommand.SYMBOL_RATE, CommonDefine.DEFAULT_INT_VALUE);
                        int frequency = CommonFunctionUnit.get_json_int(tune_param, ACSServerJsonFenceName.MqttCommand.FREQ, CommonDefine.DEFAULT_INT_VALUE);
                        int qam = CommonFunctionUnit.get_json_int(tune_param, ACSServerJsonFenceName.MqttCommand.QAM, CommonDefine.DEFAULT_INT_VALUE);
                        DeviceControlUnit.channel_scan( symbol_rate, frequency, qam );
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }break;
            case MqttCommand.CLEAR_AUTH:
            case MqttCommand.CLEAR_PROVISION:
            {
                DeviceControlUnit.clear_widevinecas_provision() ;
            }break;
            case MqttCommand.NETWORK_SPEED_TEST:
            {
                Intent intent = new Intent( BroadcastDefine.MQTT_COMMAND_BROADCAST_ACTION_BASE + MqttCommand.NETWORK_SPEED_TEST );
                CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.DO_NETWORK_SPEED_TEST, 1);
                CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, intent);
            }break;
            case MqttCommand.SIGNAL_DETECT:
            {
                Intent intent = new Intent( BroadcastDefine.MQTT_COMMAND_BROADCAST_ACTION_BASE + MqttCommand.SIGNAL_DETECT );
                CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.DO_SIGNAL_DETECT, 1);
                CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, intent);
            }break;
            case MqttCommand.PA_RESET:
            {
                CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_NO_CONNECTED_HOURS, 0);
                CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_LOCK, 0, false);
            }break;
            case MqttCommand.STATUS_UPDATE:
            {
                ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_BOX_STATUS);
            }break;
            case MqttCommand.OPEN_BROWSER:
            {
                String web_url = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.MqttCommand.ARGS, null);
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.prime.webbrowser", "com.prime.webbrowser.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("WEB_URL_KEY", web_url);
                intent.putExtra("PERMIT", true);
                ACSService.g_context.startActivity(intent);
            }break;
            default:
            {
                if (mqtt_struct_json.has(MqttCommand.TYPE) && mqtt_struct_json.has(MqttCommand.DATA))
                {
                    boolean data_type_is_jsonarray = false ;
                    String type = CommonFunctionUnit.get_json_string(mqtt_struct_json, MqttCommand.TYPE, null) ;
                    try {
                        if (mqtt_struct_json.get(MqttCommand.DATA).toString().substring(0, 1).contentEquals("[")) {
                            data_type_is_jsonarray = true ;
                        } else {
                            data_type_is_jsonarray = false ;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    Log.d( TAG, "receive mqtt type = " + type ) ;
                    Log.d( TAG, "receive mqtt data_type_is_jsonarray = " + data_type_is_jsonarray ) ;
                    Log.d( TAG, "receive mqtt mqtt_struct_json = " + mqtt_struct_json ) ;
                    if (type.contentEquals(MqttCommand.MQTT_OTA_GROUP_CHANGE))
                    {
                        JSONObject data = CommonFunctionUnit.get_json_jsonobj(mqtt_struct_json, MqttCommand.DATA, new JSONObject()) ;
                        ParseACSData.parse_mqtt_data_ota_group_change(data) ;
                        ProcessACSData.process_all_data(ProcessACSData.MQTT_DATA_OTA_GROUP_CHANGE);
                    }
                    else if (type.contentEquals(MqttCommand.MQTT_APP_GROUP_CHANGE))
                    {
                        JSONObject data = CommonFunctionUnit.get_json_jsonobj(mqtt_struct_json, MqttCommand.DATA, new JSONObject()) ;
                        HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic = CommonFunctionUnit.get_json_jsonarray( mqtt_struct_json, ACSServerJsonFenceName.ProvisionResponse.MQTT_TV_MAIL_TOPIC, HttpContentValue.ProvisionResponse.mqtt_tv_mail_topic );
                        ParseACSData.parse_mqtt_data_app_group_change(data);
                        ProcessACSData.process_all_data(ProcessACSData.MQTT_DATA_APP_GROUP_CHANGE);
                    }
                    else if (type.contentEquals(MqttCommand.MQTT_TV_MAIL))
                    {
                        ParseACSData.parse_mqtt_tv_mail(data_type_is_jsonarray, mqtt_struct_json) ;
                        ProcessACSData.process_all_data(ProcessACSData.MQTT_DATA_TV_MAIL);
                    }
                    else if(type.contentEquals(MqttCommand.MQTT_MUSIC_CATEGORY))
                    {
                        JSONObject data = CommonFunctionUnit.get_json_jsonobj(mqtt_struct_json, MqttCommand.DATA, new JSONObject()) ;
                        HttpContentValue.ProvisionResponse.music_category_rawdata = CommonFunctionUnit.get_json_jsonarray( data, ACSServerJsonFenceName.ProvisionResponse.MUSIC_CATEGORY, HttpContentValue.ProvisionResponse.music_category_rawdata);
                        CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.MUSIC_CATEGORY, HttpContentValue.ProvisionResponse.music_category_rawdata, false);
                    }
                    else if(type.contentEquals(MqttCommand.MQTT_NETWORK_QUALITY_INFO))
                    {
                        JSONObject data = CommonFunctionUnit.get_json_jsonobj(mqtt_struct_json, MqttCommand.DATA, new JSONObject()) ;
                        HttpContentValue.ProvisionResponse.network_quality_info_rawdata = CommonFunctionUnit.get_json_jsonarray( data, ACSServerJsonFenceName.ProvisionResponse.NETWORK_QUALITY_INFO, HttpContentValue.ProvisionResponse.network_quality_info_rawdata);
                        CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.NETWORK_QUALITY_INFO, HttpContentValue.ProvisionResponse.network_quality_info_rawdata, false);
                    }
                }
                else if (mqtt_struct_json.has(MqttCommand.TYPE))
                {
                    String type = CommonFunctionUnit.get_json_string(mqtt_struct_json, MqttCommand.TYPE, null) ;

                    if (type.contentEquals(MqttCommand.MQTT_ADD_TOPIC)||type.contentEquals(MqttCommand.MQTT_REMOVE_TOPIC))
                    {
                        JSONArray temp_topic ;
                        try {
                            if (mqtt_struct_json.get(ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO).toString().substring(0, 1).contentEquals("[")) {
                                temp_topic = CommonFunctionUnit.get_json_jsonarray(mqtt_struct_json, ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO, new JSONArray());
                            } else {
                                String temp_topic_str = CommonFunctionUnit.get_json_string(mqtt_struct_json, ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO, null);
                                temp_topic = new JSONArray();
                                temp_topic.put(temp_topic_str);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        if (type.contentEquals(MqttCommand.MQTT_ADD_TOPIC))
                            HttpContentValue.ProvisionResponse.mqtt_topic_info = CommonFunctionUnit.mergeJsonArrays(HttpContentValue.ProvisionResponse.mqtt_topic_info, temp_topic);
                        else
                            HttpContentValue.ProvisionResponse.mqtt_topic_info = CommonFunctionUnit.removeJsonArrayContents(HttpContentValue.ProvisionResponse.mqtt_topic_info, temp_topic);
                        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.ProvisionResponse.TOPIC_INFO,
                                HttpContentValue.ProvisionResponse.mqtt_topic_info, ()->DeviceControlUnit.resubscribe_mqtt(new int[]{MqttManager.MQTT_TOPIC_TYPE_INFO}));
                    }
                    else if (type.contentEquals(MqttCommand.MQTT_RANKING))
                    {
                        JSONArray rank_list_response = CommonFunctionUnit.get_json_jsonarray(mqtt_struct_json, "result", null) ;
                        if ( rank_list_response != null )
                        {
                            ParseACSData.parse_rank_list_response(rank_list_response);
                            ProcessACSData.process_all_data(ProcessACSData.HTTP_PROCESS_RANK_LIST_DATA);
                        }
                    }
                }
                else
                {
                    Log.d( TAG, "receive mqtt UNKNOW_COMMAND mqtt_struct_json = " + mqtt_struct_json ) ;
                }
            }break;
        }
    }
}
