# Changelog

All notable changes to the CdkFfi Swift package will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial Swift package implementation
- Core wallet functionality
- Mnemonic generation and restoration
- Mint and melt operations
- Token sending and receiving
- Local storage support
- Comprehensive test suite
- Example application
- Documentation and README

### Features
- Create and manage Cashu wallets
- Generate and restore from mnemonic phrases
- Mint ecash tokens from Lightning invoices
- Pay Lightning invoices with ecash (melt)
- Send and receive ecash tokens
- Persistent local storage
- Support for multiple currency units (sat, msat, USD, EUR)
- Error handling with Swift-native error types
- Cross-platform support (iOS, macOS, tvOS, watchOS)

### Technical Details
- Swift 5.9+ compatibility
- UniFFI-generated bindings
- Rust CDK backend integration
- Native Swift types and protocols
- Comprehensive unit tests
- Example usage code

## [1.0.0] - TBD

### Added
- First stable release of Swift CDK bindings
