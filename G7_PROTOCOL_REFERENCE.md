# Dexcom G7 Protocol Reference

> Research compiled for Hummingbird xDrip clean-room implementation

## G7 vs G6 Protocol Differences

| Aspect | G6 | G7 |
|--------|----|----|
| **Authentication** | AES-128 challenge-response | EC J-PAKE (Elliptic Curve) |
| **Crypto Library** | Custom AES | mbedtls / BouncyCastle |
| **BLE Characteristics** | 0x3534, 0x3535, 0x3538 | 0x3535 (cmd), 0x3538 (notify) |
| **Pairing Stages** | Simple bond + auth | 7+ stage certificate exchange |
| **Message Format** | Separate Tx/Rx opcodes + CRC | Same opcode both directions, no CRC |
| **Data Encryption** | Encrypted | **Unencrypted** after auth |
| **Backfill Buffer** | 3 hours | 24 hours |
| **Transmitter** | Separate, reusable | Integrated in sensor |

## BLE Communication

### GATT Characteristics
```
Service: (TBD - need to capture from real device)
├── 0x3535 - Command/Request (Write)
└── 0x3538 - Notification/Response (Notify)
```

### Framing
- 20-byte packets
- `0x20` header for segmented data
- No CRC required for most commands

## J-PAKE Authentication Protocol

### Overview
- **Protocol**: EC J-PAKE (Elliptic Curve Password Authenticated Key Exchange by Juggling)
- **Library**: Dexcom uses mbedtls; Android can use BouncyCastle
- **Stages**: Multi-stage (7+) certificate exchange and challenge-response

### Sequential Pairing Stages
1. **Initialization**: Enable notifications on 0x3535/0x3538
2. **Certificate Exchange** (Stages 0-2): Three rounds of 8-byte payload exchanges
3. **Authentication Challenge** (Stages 3-7): Seven subsequent command sequences
4. **Verification & Activation**: Final acknowledgment

### Command Opcodes
Commands follow sequential ordering: `02 03 04 05 06 07...`

Notable opcodes:
- `0x0A` - Initialization/pairing stages
- `0x0B` - Backfill data requests
- `0x0C` - Encrypted stream configuration
- `0x06`, `0x07`, `0x08` - Final activation sequence
- `0x4E` - EGlucose message (glucose reading)

## Glucose Reading Format (Opcode 0x4E)

From xDrip's `EGlucoseRxMessage.java`:
```
Fields:
- status: byte
- clock: int (sensor time)
- sequence: int
- age: int (reading age in seconds)
- glucose: int (raw value × 1000)
- state: byte (CalibrationState)
- trend: signed byte (rate of change)
- predicted_glucose: int
```

## Key Insight

**After J-PAKE authentication succeeds, glucose data flows unencrypted.** This is simpler than G6 once you get past the auth handshake.

## Dependencies for Kotlin Implementation

```kotlin
// build.gradle.kts
dependencies {
    // EC J-PAKE cryptography
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")

    // Coroutines for async BLE
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // StateFlow for connection state
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

## Proposed Architecture

```kotlin
// Core sensor interface
interface CgmSensor {
    val sensorType: SensorType
    val connectionState: StateFlow<ConnectionState>
    val glucoseReadings: Flow<GlucoseReading>

    suspend fun scan(): List<DiscoveredSensor>
    suspend fun pair(sensor: DiscoveredSensor): PairingResult
    suspend fun startSession()
    suspend fun stopSession()
    suspend fun disconnect()
}

// G7-specific implementation
class DexcomG7Sensor(
    private val bleAdapter: BleAdapter,
    private val jpakeAuth: JPakeAuthenticator
) : CgmSensor {
    // ...
}

// J-PAKE authenticator using BouncyCastle
class JPakeAuthenticator {
    private val participant: ECJPAKEParticipant

    suspend fun authenticate(
        bleConnection: BleConnection,
        pairingCode: String
    ): AuthResult
}
```

## Module Structure

```
hummingbird-g7/
├── build.gradle.kts
├── settings.gradle.kts
├── core/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── com/togetherinrange/hummingbird/cgm/
│           ├── CgmSensor.kt           # Interface
│           ├── GlucoseReading.kt      # Data class
│           ├── ConnectionState.kt     # Sealed class
│           └── SensorType.kt          # Enum
├── ble/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── com/togetherinrange/hummingbird/ble/
│           ├── BleAdapter.kt          # Android BLE wrapper
│           ├── BleConnection.kt       # Connection handle
│           └── BleScanner.kt          # Discovery
├── g7/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── com/togetherinrange/hummingbird/cgm/g7/
│           ├── DexcomG7Sensor.kt      # CgmSensor impl
│           ├── G7Constants.kt         # UUIDs, opcodes
│           ├── G7MessageParser.kt     # Protocol parsing
│           ├── JPakeAuthenticator.kt  # EC J-PAKE
│           └── messages/
│               ├── EGlucoseMessage.kt
│               ├── BackfillRequest.kt
│               └── AuthMessage.kt
└── g7/src/test/kotlin/
    └── com/togetherinrange/hummingbird/cgm/g7/
        ├── G7MessageParserTest.kt
        └── JPakeAuthenticatorTest.kt
```

## Reference Resources

### Primary Sources
- **DiaBLE Discussion #17**: https://github.com/gui-dos/DiaBLE/discussions/17
  - Most detailed G7 protocol documentation
  - J-PAKE authentication flow details

- **BouncyCastle EC J-PAKE**: https://downloads.bouncycastle.org/java/docs/bcprov-jdk18on-javadoc/org/bouncycastle/crypto/agreement/ecjpake/ECJPAKECurves.html
  - Java/Kotlin crypto implementation

### Reference Implementations
- **xDrip G7 messages**: `app/src/main/java/com/eveningoutpost/dexdrip/cgm/dex/g7/`
  - `EGlucoseRxMessage.java` - Glucose reading parser
  - `BaseMessage.java` - Message base class

- **xDrip G5/G6 state machine**: `app/src/main/java/com/eveningoutpost/dexdrip/g5model/Ob1G5StateMachine.java`
  - Reference for BLE connection flow (different protocol but similar patterns)

- **Juggluco**: https://github.com/j-kaltes/Juggluco
  - Working G7 implementation (C/C++/Java, GPL-3)

- **LoopKit G7SensorKit**: https://github.com/LoopKit/G7SensorKit
  - iOS Swift implementation (limited)

### Protocol Research
- **J-PAKE Wikipedia**: https://en.wikipedia.org/wiki/Password_Authenticated_Key_Exchange_by_Juggling
- **mbedtls ecjpake.h**: https://github.com/Mbed-TLS/mbedtls (reference C implementation)

## Testing Strategy

### Unit Tests (No Hardware)
- Message parsing/serialization
- J-PAKE crypto (known test vectors)
- State machine transitions

### Integration Tests (Requires G7)
- BLE scanning and discovery
- Full authentication handshake
- Glucose reading reception
- Backfill data retrieval
- Reconnection after signal loss

## Open Questions

1. **Exact GATT Service UUID** - Need to capture from real G7
2. **J-PAKE curve parameters** - Which EC curve does Dexcom use?
3. **Certificate format** - What's exchanged in stages 0-2?
4. **Keep-alive requirements** - Does G7 need periodic pings?
5. **Backfill command format** - Exact opcode and parameters

## Next Steps

1. Set up Kotlin multi-module Gradle project
2. Implement BLE abstraction layer
3. Implement message parser for EGlucose (0x4E)
4. Implement J-PAKE authenticator with BouncyCastle
5. Capture real G7 BLE traffic to fill in unknowns
6. Integration test with real sensor
