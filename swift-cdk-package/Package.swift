// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "CdkFfi",
    platforms: [
        .iOS(.v13),
        .macOS(.v10_15),
        .tvOS(.v13),
        .watchOS(.v6)
    ],
    products: [
        // Products define the executables and libraries a package produces, making them visible to other packages.
        .library(
            name: "CdkFfi",
            targets: ["CdkFfi"]
        ),
        .executable(
            name: "CdkFfiExample",
            targets: ["CdkFfiExample"]
        )
    ],
    dependencies: [
        // Dependencies declare other packages that this package depends on.
    ],
    targets: [
        // Targets are the basic building blocks of a package, defining a module or a test suite.
        .target(
            name: "CdkFfiC",
            dependencies: [],
            path: "Sources/CdkFfiC",
            sources: ["cdk_ffiFFI.h"],
            publicHeadersPath: ".",
            cSettings: [
                .headerSearchPath("."),
                .define("SWIFT_PACKAGE")
            ],
            linkerSettings: [
                .linkedLibrary("cdk_ffi"),
                .unsafeFlags(["-L../Resources"])
            ]
        ),
        .target(
            name: "CdkFfi",
            dependencies: ["CdkFfiC"],
            path: "Sources/CdkFfi"
        ),
        .executableTarget(
            name: "CdkFfiExample",
            dependencies: ["CdkFfi"],
            path: "Sources/CdkFfiExample"
        ),
        .testTarget(
            name: "CdkFfiTests",
            dependencies: ["CdkFfi"]
        )
    ]
)
