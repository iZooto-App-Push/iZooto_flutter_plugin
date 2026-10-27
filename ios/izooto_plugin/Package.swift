// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "izooto_plugin",

    platforms: [
        .iOS("13.0")
    ],

    products: [
        .library(
            name: "izooto-plugin",
            targets: ["izooto_plugin"]
        )
    ],

    dependencies: [
        .package(
            name: "FlutterFramework",
            path: "../FlutterFramework"
        ),
        .package(
            url: "https://github.com/iZooto-App-Push/iZooto-XCFramework.git",
            exact: "3.2.4"
        ),
    ],

    targets: [
        .target(
            name: "izooto_plugin",
            dependencies: [
                .product(
                    name: "FlutterFramework",
                    package: "FlutterFramework"
                ),
                .product(
                    name: "iZooto-XCFramework",
                    package: "iZooto-XCFramework"
                ),
            ]
        )
    ]
)
