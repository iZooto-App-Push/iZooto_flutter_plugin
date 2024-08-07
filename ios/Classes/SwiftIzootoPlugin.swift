import Flutter
import UIKit
import iZootoiOSSDK
import UserNotifications
@objc public class SwiftIzootoPlugin: NSObject, FlutterPlugin,UNUserNotificationCenterDelegate, iZootoNotificationOpenDelegate, iZootoLandingURLDelegate {
    
    static var data = "";
    
    internal init(channel: FlutterMethodChannel) {
        self.channel = channel
    }
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: AppConstant.IZ_PLUGIN_NAME, binaryMessenger: registrar.messenger())
        let instance = SwiftIzootoPlugin(channel: channel)
        registrar.addApplicationDelegate(instance)
        registrar.addMethodCallDelegate(instance, channel: channel)
        let center = UNUserNotificationCenter.current()
        center.delegate = instance
    }
    public func onNotificationOpen(action: Dictionary<String, Any>) {
        let jsonData = try! JSONSerialization.data(withJSONObject: action, options: [])
        let decoded = String(data: jsonData, encoding: .utf8)!
        self.channel.invokeMethod(AppConstant.IZ_PLUGIN_OPEN_NOTIFICATION, arguments: decoded)
        
    }
    public func onHandleLandingURL(url: String) {
        self.channel.invokeMethod(AppConstant.IZ_PLUGIN_HANDLE_LANDING_URL, arguments: url)
    }
    var channel = FlutterMethodChannel()
    var launchNotification: [String: Any]?
    var resumingFromBackground = false
    static var loggingEnabled = false
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult)
     {
    switch (call.method)
        {
          case AppConstant.IZ_PLUGIN_INITIALISE:
              guard let map = call.arguments as? Dictionary<String, String>,
                let appId = map[AppConstant.IZ_PLUGIN_APP_ID] else {
              print("Error: 'map' is not a Dictionary<String, String> or 'appId' is nil")
              return
              }
          
              let iZootoInitSettings = [AppConstant.IZ_PLUGIN_AUTO_PROMPT: true,AppConstant.IZ_PLUGIN_IS_WEBVIEW: true,AppConstant.IZ_PLUGIN_PROVISIONAL_AUTH:false]
              iZooto.setPluginVersion(pluginVersion: AppConstant.IZ_PLUGIN_VERSION)
              iZooto.initialisation(izooto_id: appId, application: UIApplication.shared,  iZootoInitSettings:iZootoInitSettings)
              iZooto.notificationOpenDelegate = self
              iZooto.landingURLDelegate = self
              UNUserNotificationCenter.current().delegate = self
            
            break;
            
          case AppConstant.IZ_PLUGIN_GET_PLATEFORM_VERSION:
            result("iOS " + UIDevice.current.systemVersion)
            break;
            
          case AppConstant.IZ_PLUGIN_ADD_EVENTS:
           if let map = call.arguments as? Dictionary<String, String>,
             let eventName = map[AppConstant.IZ_PLUGIN_EVENT_NAME] {
              iZooto.addEvent(eventName: eventName, data: map)
          } else {
              print("Error: 'map' is not a Dictionary<String, String> or 'eventName' is nil")
          }
            break;
         case AppConstant.IZ_PLUGIN_ADD_USER_PROPERTIES:
           if let userPropertiesData = call.arguments as? Dictionary<String, String>,
              let keyName = userPropertiesData[AppConstant.IZ_PLUGIN_PROPERTIES_KEY],
              let valueName = userPropertiesData[AppConstant.IZ_PLUGIN_PROPERTIES_VALUE] {
              let data = [keyName: valueName]
              iZooto.addUserProperties(data: data)
          } else {
              print("Error: 'call.arguments' is not a Dictionary<String, String> or 'keyName' or 'valueName' is nil")
          }
            break;
         case AppConstant.IZ_PLUGIN_SET_SUBSCRIPTION:
              guard let enable = call.arguments as? Bool else {
              print("Error: 'enable' is not a Boolean")
              return
             }
            print(enable)

              iZooto.setSubscription(isSubscribe: enable)
       break;
            
         case AppConstant.IZ_PLUGIN_NAVIGATE_TO_SETTING :
            iZooto.navigateToSettings()
            break;
         case AppConstant.IZ_PLUGIN_GET_NOTIFICATION_FEED:
            let map = call.arguments as? Dictionary<String, Any>
            let enable: Bool = (map?[AppConstant.IZ_PLUGIN_IS_PAGINATION] as? Bool ?? false)
            iZooto.getNotificationFeed(isPagination: enable){ (jsonString, error) in
                if error != nil {
                    result(AppConstant.IZ_PLUGIN_NO_MORE_DATA)
                } else if let jsonString = jsonString {
                    result(jsonString)
                }
            }
            break;

        case AppConstant.IZ_GOOGLE_ONE_TAP :
           let map = call.arguments as? Dictionary<String, Any>
          guard map != nil else {
              print("Error: 'map' is nil")
              return
          }
          guard let emailData = map?["email"] as? String, !emailData.isEmpty else {
              print("Error: 'email' is nil or blank")
              return
          }
          let fullName = map?["fName"] as? String
          let lName = map?["lName"] as? String
          iZooto.syncUserDetailsEmail(email: emailData, fName: fullName ?? "", lName: lName ?? "")
       break;


         default:
            result(AppConstant.IZ_PLUGIN_NOT_RESULT)
            break;
     }
    }
    
    // appDelegate integration
    public func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [AnyHashable : Any] = [:]) -> Bool {
        
        launchNotification = launchOptions[UIApplication.LaunchOptionsKey.remoteNotification] as? [String: Any]
        
        return true
    }
    
    
    //received apns token
    public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        iZooto.getToken(deviceToken: deviceToken)
        let tokenParts = deviceToken.map { data -> String in
            return String(format: "%02.2hhx", data)
        }
        let token = tokenParts.joined()
        channel.invokeMethod(AppConstant.IZ_PLUGIN_TOKEN_RECEIVED, arguments: token)
    }
    
    
    public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) -> Bool {
       return true
    }
    // called forground
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        let jsonData = try! JSONSerialization.data(withJSONObject: userInfo, options: [])
        let decoded = String(data: jsonData, encoding: .utf8)!
        channel.invokeMethod(AppConstant.IZ_PLUGIN_RECEIVED_PAYLOAD, arguments: decoded)
        iZooto.handleForeGroundNotification(notification: notification, displayNotification: AppConstant.IZ_PLUGIN_IS_NONE, completionHandler:completionHandler)
        completionHandler([.alert,.badge,.sound])
    }
    // called background
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        let jsonData = try! JSONSerialization.data(withJSONObject: userInfo, options: [])
        let decoded = String(data: jsonData, encoding: .utf8)!
        channel.invokeMethod(AppConstant.IZ_PLUGIN_RECEIVED_PAYLOAD, arguments: decoded);
        iZooto.notificationHandler(response: response)
        completionHandler()
    }
    
    func onResume(userInfo: [AnyHashable: Any]) {
        if launchNotification != nil {
            channel.invokeMethod(AppConstant.IZ_PLUGIN_ON_RESUME, arguments: userInfo)
            self.launchNotification = nil
            return
        }
        // iZooto.notificationOpenDelegate=self
        channel.invokeMethod(AppConstant.IZ_PLUGIN_ON_RESUME, arguments: userInfo)
    }
    
    
}
