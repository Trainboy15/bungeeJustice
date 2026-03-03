# BungeeJustice Test Suite

This project now includes comprehensive unit tests using **JUnit 4** and **Mockito** to test all commands and core functionality.

## Overview

Tests are automatically run during the `mvn package` process using Maven Surefire plugin. The test suite covers:

- **PunishCommand**: Tests for ban, tempban, mute, tempmute, ipban, ipmute commands
- **UnpunishCommand**: Tests for removal by punishment ID
- **SimpleCommands**: Tests for kick, warn, note commands
- **PunishmentManager**: Core business logic tests
- **Punishment**: Data model validation tests

## Test Files

Located in `src/test/trainboy888/`:

1. **CommandTestBase.java** - Base class with common setup and helper methods for all command tests
2. **PunishCommandTest.java** - Tests for punishment application commands (~50 test cases)
3. **UnpunishCommandTest.java** - Tests for punishment removal by ID (~10 test cases)
4. **SimpleCommandsTest.java** - Tests for kick/warn/note commands (~12 test cases)
5. **PunishmentManagerTest.java** - Core manager logic tests (~20 test cases)
6. **PunishmentTest.java** - Data model tests (~15 test cases)

## Running Tests

### Run all tests
```bash
mvn test
```

### Run tests as part of package build
```bash
mvn package
```
Tests are automatically run and must pass before the JAR is created.

### Run specific test class
```bash
mvn test -Dtest=PunishCommandTest
```

### Run specific test method
```bash
mvn test -Dtest=PunishCommandTest#testBanCommandSuccess
```

### Skip tests (if needed)
```bash
mvn package -DskipTests
```

## Test Coverage

The test suite covers:

✅ **Permission checking** - Verifying commands check permissions correctly  
✅ **Argument validation** - Testing insufficient/invalid arguments  
✅ **Punishment creation** - Verifying punishments are created with correct types and durations  
✅ **Punishment removal** - Testing removal by ID and various removal methods  
✅ **Player name resolution** - Testing UUID to name conversion  
✅ **IP normalization** - Testing IP address normalization  
✅ **Duration parsing** - Testing various time format parsing (30m, 1h, 1d, 2w)  
✅ **Expiration logic** - Testing permanent vs temporary/expired punishments  
✅ **Message formatting** - Verifying correct messages are sent to players

## Mock Objects

The test framework mocks:
- **CommandSender** - Command executor
- **PunishmentManager** - Punishment storage/retrieval
- **MessageConfig** - Message configuration
- **OfflineNameResolver** - Name/UUID resolution
- **ProxiedPlayer** - Online players (for kick command)

## Example Test

```java
@Test
public void testBanCommandSuccess() {
    String[] args = {TEST_PLAYER_NAME, "Cheating"};
    banCommand.execute(sender, args);

    assertPunishmentApplied(PunishmentType.BAN);
    assertMessageSent();
}
```

## CI/CD Integration

To integrate tests into your CI/CD pipeline (GitHub Actions, GitLab CI, etc.):

```bash
# Your CI script
mvn clean package

# If build succeeds, tests passed!
if [ $? -eq 0 ]; then
    echo "✓ All tests passed"
    # Deploy JAR
fi
```

## Development

When adding new commands:
1. Create corresponding test class in `src/test/trainboy888/`
2. Extend `CommandTestBase` for common setup
3. Write test methods following existing patterns
4. Run `mvn test` to verify

## Maven Surefire Configuration

The pom.xml is configured with:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <testSourceDirectory>src/test</testSourceDirectory>
    </configuration>
</plugin>
```

This automatically discovers and runs all test classes ending in `*Test.java` in the test directory.

## Troubleshooting

**Tests won't compile:**
- Ensure `src/test/trainboy888/` directory exists
- Check that all import statements are correct
- Verify JUnit and Mockito are in dependencies

**Tests fail:**
- Check mock setup in `CommandTestBase.setupBase()`
- Verify mock message keys match config.yml
- Ensure test data (TEST_PLAYER_UUID, etc.) is initialized

**Slow test execution:**
- Tests use in-memory mocks, should be fast
- If slow, check for blocking file I/O or network calls
- Consider using @Test(timeout = 5000) to catch hanging tests
