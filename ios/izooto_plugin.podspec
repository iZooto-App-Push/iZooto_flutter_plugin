#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint izooto_plugin.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'izooto_plugin'
  s.version          = '2.8.4'
  s.summary          = 'The iZooto Flutter SDK'
  s.description      = 'Allows you to easily add iZooto to your Flutter projects, to make sending and handling push notifications easy.'
  s.homepage         = 'http://izooto.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Rambali Kumar' => 'rambali@izooto.com'}
  s.source           = { :path => '.' }

  s.source_files = [
    'Classes/**/*.{h,m}',
    'izooto_plugin/Sources/izooto_plugin/**/*.swift'
  ]

  s.public_header_files = 'Classes/**/*.h'

  s.dependency 'Flutter'
  s.dependency 'iZootoiOSSDK'

  s.platform = :ios, '12.0'
  s.ios.deployment_target = '12.0'

  s.static_framework = true

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }

  s.swift_version = '5.0'
end
