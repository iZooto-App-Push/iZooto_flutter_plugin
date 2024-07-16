package com.izooto_plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.izooto.AppConstant;
import com.izooto.NotificationHelperListener;
import com.izooto.NotificationReceiveHybridListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.OneTapCallback;
import com.izooto.Payload;
import com.izooto.PreferenceUtil;
import com.izooto.PushTemplate;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

@SuppressWarnings("IzootoPlugin")
public class IzootoPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    @SuppressLint("StaticFieldLeak")
    static Context context;
    Activity activity;
    MethodChannel channel;
    private String notificationOpenedData, notificationToken, notificationWebView, notificationPayload;
    private String iz_email, iz_firstName, iz_lastName;

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    }

    @Override
    public void onDetachedFromActivity() {
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), iZootoConstant.IZ_PLUGIN_NAME); //define the chanel name
        channel.setMethodCallHandler(this);

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    // Handle the all methods
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        iZootoNotificationListener iZootoNotificationListener = new iZootoNotificationListener();
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        switch (call.method) {
            case iZootoConstant.IZ_ANDROID_INIT:
                iZooto.isHybrid = true;
                try {
                    preferenceUtil.setBooleanData(AppConstant.IZ_DEFAULT_WEB_VIEW, (boolean) call.arguments);
                    if (preferenceUtil.getBoolean(AppConstant.IZ_DEFAULT_WEB_VIEW)) {
                        iZooto.initialize(context)
                                .setTokenReceivedListener(iZootoNotificationListener)
                                .setNotificationReceiveListener(iZootoNotificationListener)
                                .setNotificationReceiveHybridListener(iZootoNotificationListener)
                                .build();
                    } else {
                        iZooto.initialize(context)
                                .setTokenReceivedListener(iZootoNotificationListener)
                                .setNotificationReceiveListener(iZootoNotificationListener)
                                .setLandingURLListener(iZootoNotificationListener)
                                .setNotificationReceiveHybridListener(iZootoNotificationListener)
                                .build();
                    }
                    iZooto.setPluginVersion(iZootoConstant.IZ_PLUGIN_VERSION);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_SET_SUBSCRIPTION:
                try {
                    boolean setSubscription = (boolean) call.arguments;
                    iZooto.setSubscription(setSubscription);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_FIREBASE_ANALYTICS:
                try {
                    boolean trackFirebaseAnalytics = (boolean) call.arguments;
                    iZooto.setFirebaseAnalytics(trackFirebaseAnalytics);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_ADD_EVENTS:
                try {
                    String eventName = call.argument(iZootoConstant.IZ_EVENT_NAME);
                    HashMap<String, Object> hashMapEvent = new HashMap<>();
                    hashMapEvent = call.argument(iZootoConstant.IZ_EVENT_VALUE);
                    iZooto.addEvent(eventName, hashMapEvent);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_ADD_PROPERTIES:
                try {
                    HashMap<String, Object> hashMapUserProperty = new HashMap<>();
                    hashMapUserProperty = (HashMap<String, Object>) call.arguments;
                    iZooto.addUserProperty(hashMapUserProperty);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_NOTIFICATION_SOUND:
                try {
                    String soundName = (String) call.arguments;
                    iZooto.setNotificationSound(soundName);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_ADD_TAGS:
                try {
                    List<String> addTagList = new ArrayList<>();
                    addTagList = (List<String>) call.arguments;
                    iZooto.addTag(addTagList);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_REMOVE_TAG:
                try {
                    List<String> addTagList = new ArrayList<>();
                    addTagList = (List<String>) call.arguments;
                    iZooto.removeTag(addTagList);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_DEFAULT_TEMPLATE:
                try {
                    int notificationTemplate = call.argument(iZootoConstant.IZ_DEFAULT_TEMPLATE);
                    setCustomNotification(notificationTemplate);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_DEFAULT_NOTIFICATION_BANNER:
                try {
                    String notificationTemplateBanner = call.argument(iZootoConstant.IZ_DEFAULT_NOTIFICATION_BANNER);
                    if (getBadgeIcon(context, notificationTemplateBanner) != 0) {
                        iZooto.setDefaultNotificationBanner(getBadgeIcon(context, notificationTemplateBanner));
                    }
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_HANDLE_NOTIFICATION:
                try {
                    Object handleNotification = call.argument(iZootoConstant.IZ_HANDLE_NOTIFICATION);
                    Map<String, String> map = (Map<String, String>) handleNotification;
                    if (map != null && !map.isEmpty()) {
                        iZooto.iZootoHandleNotification(context, map);
                    }
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_RECEIVED_PAYLOAD:
                try {
                    iZootoNotificationListener.onNotificationReceivedHybrid(notificationPayload);
                    iZooto.notificationReceivedCallback(iZootoNotificationListener);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_OPEN_NOTIFICATION:
                try {
                    iZootoNotificationListener.onNotificationOpened(notificationOpenedData);
                    iZooto.notificationClick(iZootoNotificationListener);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_DEVICE_TOKEN:
                try {
                    iZootoNotificationListener.onTokenReceived(notificationToken);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            case iZootoConstant.IZ_HANDLE_WEB_VIEW:
                if (!preferenceUtil.getBoolean(AppConstant.IZ_DEFAULT_WEB_VIEW)) {
                    try {
                        iZootoNotificationListener.onWebView(notificationWebView);
                        iZooto.notificationWebView(iZootoNotificationListener);
                    } catch (Exception ex) {
                        Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                    }
                }
                break;

            case iZootoConstant.IZ_CHANNEL_NAME:
                try {
                    String channelName = (String) call.arguments;
                    iZooto.setNotificationChannelName(channelName);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            /*   navigateToSettings    */
            case iZootoConstant.IZ_NAVIGATE_SETTING:
                try {
                    iZooto.navigateToSettings(activity);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            // Android 13 - Notification permission
            case iZootoConstant.IZ_NOTIFICATION_PERMISSION:
                try {
                    if (Build.VERSION.SDK_INT >= 33) {
                        iZooto.promptForPushNotifications();
                    } else {
                        Log.i(iZootoConstant.IZ_NOTIFICATION_PERMISSION, iZootoConstant.IZ_API_LEVEL_ERROR);
                    }
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            // Notification Feed data
            case iZootoConstant.IZ_NOTIFICATION_DATA:
                try {
                    boolean isPagination = Boolean.TRUE.equals(call.argument(iZootoConstant.IZ_IS_PAGINATION));
                    String centerFeedData = iZooto.getNotificationFeed(isPagination);
                    result.success(centerFeedData);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            // OneTap Activity Request
            case iZootoConstant.IZ_REQUEST_ONE_TAP_ACTIVITY:
                try {
                    if (LibGuard.hasOneTapLibrary()) {
                        iZooto.requestOneTapActivity(activity, iZootoNotificationListener);
                    } else {
                        Log.e(AppConstant.APP_NAME_TAG, "OneTap initialization failed due to missing libraries. Please check the library configuration.");
                    }
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            // OneTapCallback response
            case iZootoConstant.IZ_ONE_TAP_CALLBACK:
                try {
                    if (LibGuard.hasOneTapLibrary()) {
                        iZootoNotificationListener.syncOneTapResponse(iz_email, iz_firstName, iz_lastName);
                    }
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            // To Get user details
            case iZootoConstant.IZ_SYNC_USER_DETAILS:
                try {
                    String email = call.argument(iZootoConstant.IZ_EMAIL);
                    String firstName = call.argument(iZootoConstant.IZ_FIRST_NAME);
                    String lastName = call.argument(iZootoConstant.IZ_LAST_NAME);
                    iZooto.syncUserDetailsEmail(context, email, firstName, lastName);
                } catch (Exception ex) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, ex.toString());
                }
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    private class iZootoNotificationListener implements TokenReceivedListener, NotificationHelperListener, NotificationWebViewListener, NotificationReceiveHybridListener, OneTapCallback {
        @Override
        public void onNotificationReceived(Payload payload) {
            if (payload != null) {
                Gson gson = new Gson();
                String jsonPayload = gson.toJson(payload);
                try {
                    invokeMethodOnUiThread(iZootoConstant.IZ_RECEIVED_PAYLOAD, jsonPayload);
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }

        @Override
        public void onNotificationOpened(String data) {
            notificationOpenedData = data;
            if (data != null) {
                try {
                    invokeMethodOnUiThread(iZootoConstant.IZ_OPEN_NOTIFICATION, data);
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }

        @Override
        public void onTokenReceived(String token) {
            notificationToken = token;
            if (token != null) {
                try {
                    invokeMethodOnUiThread(iZootoConstant.IZ_DEVICE_TOKEN, token);
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }

        @Override
        public void onWebView(String landingUrl) {
            notificationWebView = landingUrl;
            if (landingUrl != null) {
                try {
                    invokeMethodOnUiThread(iZootoConstant.IZ_HANDLE_WEB_VIEW, landingUrl);
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }

        @Override
        public void onNotificationReceivedHybrid(String receiveData) {
            notificationPayload = receiveData;
            if (receiveData != null) {
                try {
                    JSONArray listArray = new JSONArray(receiveData);
                    JSONArray reverseList = new JSONArray();
                    if (listArray.length() > 0) {
                        reverseList.put(listArray.getJSONObject(listArray.length() - 1));
                    }

                    invokeMethodOnUiThread(iZootoConstant.IZ_RECEIVED_PAYLOAD, reverseList.toString());
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }

        @Override
        public void syncOneTapResponse(String email, String firstName, String lastName) {
            iz_email = email;
            iz_firstName = firstName;
            iz_lastName = lastName;
            if (email != null && firstName != null && lastName != null) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(iZootoConstant.IZ_EMAIL, email);
                    jsonObject.put(iZootoConstant.IZ_FIRST_NAME, firstName);
                    jsonObject.put(iZootoConstant.IZ_LAST_NAME, lastName);
                    invokeMethodOnUiThread(iZootoConstant.IZ_ONE_TAP_CALLBACK, jsonObject.toString());
                } catch (Exception e) {
                    Log.v(iZootoConstant.IZ_PLUGIN_EXCEPTION, e.toString());
                }
            }
        }
    }

    private void runOnMainThread(final Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread())
            runnable.run();
        else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runnable);
        }
    }

    void invokeMethodOnUiThread(final String methodName, final String map) {
        final MethodChannel channel = this.channel;
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod(methodName, map);
            }
        });
    }

    private static void setCustomNotification(int index) {
        if (index == 2) {
            iZooto.setDefaultTemplate(PushTemplate.TEXT_OVERLAY);
        } else if (index == 3) {
            iZooto.setDefaultTemplate(PushTemplate.DEVICE_NOTIFICATION_OVERLAY);
        } else {
            iZooto.setDefaultTemplate(PushTemplate.DEFAULT);
        }
    }

    static int getBadgeIcon(Context context, String setBadgeIcon) {
        int bIicon = 0;
        try {
            @SuppressLint("DiscouragedApi") int checkExistence = context.getResources().getIdentifier(setBadgeIcon, "drawable", context.getPackageName());
            if (checkExistence != 0) {  // the resource exists...
                bIicon = checkExistence;
            } else {  // checkExistence == 0  // the resource does NOT exist!!
                int checkExistenceMipmap = context.getResources().getIdentifier(setBadgeIcon, "mipmap", context.getPackageName());
                if (checkExistenceMipmap != 0) {  // the resource exists...
                    bIicon = checkExistenceMipmap;
                }
            }
        } catch (Exception e) {
            Log.v(AppConstant.APP_NAME_TAG, e.toString());
        }
        return bIicon;
    }
}
