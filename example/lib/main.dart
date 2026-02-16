import 'dart:collection';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:izooto_plugin/iZooto_flutter.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:convert';

void main() => runApp(MyApp());
class MyApp extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            title: 'Flutter Demo',
            theme: ThemeData(
                primarySwatch: Colors.blue,
            ),
            home: Home(title: 'HomePage'),
            routes: {
                'pageTwo': (context) => PageTwo(title: 'Page Two'),
            },
        );
    }
}

class Home extends StatefulWidget {
     final String title;
     Home({required this.title});
      @override
      _HomeState createState() => new _HomeState();
}

class _HomeState extends State<Home> {
      static const platform = const MethodChannel("iZooto-flutter");
      @override
      void initState() {
        super.initState();
        initMethod();
      }

      @override
      void dispose() {
        super.dispose();
      }

      @override
      Widget build(BuildContext context) {
          return Scaffold(
              appBar: AppBar(
                  title: Text(widget.title),
                ),
              body: const Center(
                  child: Text(
                    'HomePage',
                     style: TextStyle(color: Colors.green, fontSize: 20),
                  ),
              ),
              floatingActionButton: FloatingActionButton(
                  autofocus: true,
                  focusElevation: 5,
                  child: const Icon(Icons.notifications),
                  onPressed: () {
                    Navigator.push(context,
                    MaterialPageRoute(builder: (context) => const SecondRoute()));
                    // iZooto.navigateToSettings();
                   // iZooto.setSubscription(false);
                      iZooto.addTag(["Delhi"]);

                    print("Unsuscribe false");
                  },
              ),
          );
      }

      void initMethod() async {
          iZooto.androidInit(false);
          iZooto.promptForPushNotifications();
         // iZooto.addUserProperty("android","developer");
          // iZooto.setDefaultTemplate(PushTemplate.TEXT_OVERLAY);
          // iZooto.setNotificationChannelName("Welcome to iZooto");

          // OneTap Method call
          iZooto.requestOneTapActivity();
          iZooto.shared.oneTapResponse((response) {
            print('DATA -> ' +response.toString());
            // Parse the JSON string
            Map<String, dynamic> jsonString = jsonDecode(response.toString());
            // Retrieve values
            String email = jsonString['email'];
            String firstName = jsonString['firstName'];
            String lastName = jsonString['lastName'];
            // Print the values
            print('Email: $email');
            print('First Name: $firstName');
            print('Last Name: $lastName');
          });

           iZooto.syncUserDetailsEmail("abc1@gmail.com", "Test1", "Demo1");

          if (Platform.isIOS) {
              iZooto.iOSInit(
              appId: "6d6292461db5888f4c10172c0767541a40e2175c");       // for iOS
          }
          // var data = await iZooto.getNotificationFeed(false);
          //            print(' >> $data'); 9f42c47c6d270255327c057ba31621cbd98ea12f
          iZooto.shared.onNotificationReceived((payload) {        // Received payload Android/iOS
              print('PayLoad >>  $payload');
          });

          iZooto.shared.onNotificationOpened((data) {     // DeepLink Android/iOS
              print('Data >>  $data');
              Navigator.push(context,
              MaterialPageRoute(builder: (context) => const SecondRoute()));
          });

          iZooto.shared.onWebView((landingUrl) {      //LandingURLDelegate Android/iOS
              print('Landing URL >>  $landingUrl');
              Navigator.push(context,
              MaterialPageRoute(builder: (context) => const SecondRoute()));
          });

          iZooto.shared.onTokenReceived((token) {     // Device token Android/iOS
              print('Token >>  $token ');
          });

          try {           //iOS DeepLink Killed state code
              String value = await platform.invokeMethod("OpenNotification");
              if (value != null) {
                  print('iZooto Killed state ios data : $value ');
                  Navigator.push(context, MaterialPageRoute(builder: (context) => const SecondRoute()));
                  // Navigator.of(context).pushNamed('pageTwo');
              }
          } catch (e) {}
      }

}

Future<dynamic> onPush(String name, Map<String, dynamic> payload) {
      return Future.value(true);
}

// Future<dynamic> _onBackgroundMessage(Map<String, dynamic> data) =>
//     onPush('onBackgroundMessage', data);

class PageTwo extends StatefulWidget {
      final String title;
      PageTwo({required this.title});
      @override
      _PageTwoState createState() => new _PageTwoState();
}

class _PageTwoState extends State<PageTwo> {
      @override
      Widget build(BuildContext context) {
         return Scaffold(
            appBar: AppBar(
              title: Text(widget.title),
              ),
            body: Center(
              child: Text('Page Two'),
              ),
            floatingActionButton: FloatingActionButton(
              autofocus: true,
              focusElevation: 5,
              child: const Icon(Icons.notifications),
              onPressed: () {
                // iZooto.setNewsHub();
              },
            ),
         );
      }
}

class SecondRoute extends StatelessWidget {
  const SecondRoute();
    @override
    Widget build(BuildContext context) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Second Page'),
            ),
          body: Center(
            child: ElevatedButton(
              onPressed: () {
               // iZooto.setSubscription(true);
                Navigator.pop(context);
                print("Subscribe True");
              },
              child: const Text('Go back!'),
            ),
          ),
        );
    }
}