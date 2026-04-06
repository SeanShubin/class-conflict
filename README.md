# Class Conflict Detector

A quality metric tool that scans Maven artifacts (.jar and .zip files) for class files with the same fully qualified name but different bytecode.

## Purpose

When multiple versions of the same class exist in your classpath with different bytecode, the JVM will load whichever one it encounters first, leading to unpredictable behavior. This tool helps identify such conflicts before they cause runtime issues.

## Features

- Scans .jar and .zip files recursively (including nested archives)
- Detects duplicate class names with different bytecode
- Computes SHA-256 hashes to identify bytecode differences  
- Generates structured reports in three formats:
  - **count/**: Machine-readable metrics (JSON)
  - **diff/**: Per-conflict details (JSON)
  - **browse/**: Human-readable reports (text)
- Console output for immediate feedback
- Exit code suitable for CI/CD integration

## Building

```bash
mvn clean package
```

## Usage

```bash
java -jar console/target/classconflict-console.jar [--output-dir <dir>] <artifact1> [artifact2] [artifact3] ...
```

### Options

- `--output-dir <dir>`: Directory for detailed reports (default: `generated/class-conflict`)

### Example

```bash
java -jar console/target/classconflict-console.jar app.jar lib1.jar lib2.jar
```

With custom output directory:
```bash
java -jar console/target/classconflict-console.jar --output-dir build/reports app.jar lib1.jar lib2.jar
```

### Output Structure

The tool generates three types of reports:

#### 1. Count Report (`count/quality-metrics.json`)
Machine-readable summary for CI/CD integration:
```json
{
  "classesScanned" : 150,
  "conflictsFound" : 2
}
```

#### 2. Diff Reports (`diff/conflict-<classname>.json`)
Per-conflict details with artifact locations and hashes:
```json
{
  "fullyQualifiedName" : "com.example.MyClass",
  "instances" : [
    {
      "artifact" : "app-1.0.jar",
      "hash" : "ae21bfae039214e6179e23ae1a785f55777929b8ee2baf872901c4ef226194eb"
    },
    {
      "artifact" : "lib-2.0.jar",
      "hash" : "5127924123492d9e4d98783f0fac6a0ce3c87d833517e4fff490333282bc4519"
    }
  ]
}
```

#### 3. Browse Reports (`browse/`)
Human-readable text files:
- `summary.txt`: Overview grouped by package
- `conflict-<classname>.txt`: Detailed analysis for each conflict with resolution guidance

### Exit Codes

- `0`: No conflicts found
- `1`: Conflicts detected or invalid usage

### Console Output

```
Class Conflict Report
================================================================================

Classes scanned: 2
Conflicts found: 1

Conflicts:
--------------------------------------------------------------------------------

Class: com.example.MyClass
  Found in 2 different versions:
    1. app1.jar
       Hash: ae21bfae...
    2. app2.jar
       Hash: 5127924...

Detailed reports written to:
  Count:  generated/class-conflict/count/quality-metrics.json
  Diff:   generated/class-conflict/diff/
  Browse: generated/class-conflict/browse/
```

## Integration with Build Tools

### CI/CD Integration

Check the exit code and parse `count/quality-metrics.json`:

```bash
java -jar classconflict-console.jar *.jar
EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
  echo "Class conflicts detected!"
  cat generated/class-conflict/count/quality-metrics.json
  exit 1
fi
```

### Maven Integration

Add to your build process:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>verify</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>java</executable>
                <arguments>
                    <argument>-jar</argument>
                    <argument>${project.basedir}/tools/classconflict-console.jar</argument>
                    <argument>${project.build.directory}/${project.build.finalName}.jar</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Architecture

This project follows the maintainable code patterns from `/Users/seashubi/gitlab.cj.dev/training/maintainable-code`:

### Modules

- **console**: Entry point and production integrations
- **composition**: Dependency injection wiring and the `Integrations` interface
- **domain-api**: Business interfaces and domain models
- **domain-impl**: Business logic implementations
- **domain-test**: Test infrastructure and orchestrator
- **di-contract**: Dependency inversion boundary interfaces
- **di-delegate**: Production implementations of contracts
- **di-test**: Test fake base classes
- **zip**: ZIP/JAR file handling utilities (from kotlin-reusable)

### Key Design Patterns

1. **Composition Pipeline**: Separates wiring (constructors) from work (methods)
2. **Dependency Injection**: All external dependencies injected through the `Integrations` interface
3. **Test Orchestrator**: Tests use domain-focused APIs hiding infrastructure complexity
4. **Quality Metric Reporting**: count/diff/browse structure matching code-structure and inversion-guard

## Interpreting Results

### What Conflicts Mean

When the same class appears with different bytecode:
- The JVM loads the first version it encounters
- Behavior depends on classpath order
- Can cause:
  - NoSuchMethodError at runtime
  - ClassCastException
  - Unexpected behavior due to different implementations

### Common Causes

1. **Dependency version conflicts**: Different transitive dependencies include different versions
2. **Shaded/relocated artifacts**: Multiple artifacts include the same dependency
3. **Build inconsistencies**: JAR built with different versions of dependencies

### Resolution Steps

1. Examine the `browse/conflict-<classname>.txt` files for detailed analysis
2. Use `mvn dependency:tree` to identify dependency sources
3. Add `<exclusions>` or use dependency management to force a single version
4. Consider using Maven Enforcer Plugin's dependency convergence rule

## License

Unlicense
