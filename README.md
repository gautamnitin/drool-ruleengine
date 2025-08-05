# Rule Engine Framework

This project provides a flexible rule engine framework that supports multiple rule engine implementations. The default implementation uses Drools, but the architecture allows for easy integration of alternative rule engines.

## Features

- Abstract rule engine interface for implementation flexibility
- Default Drools-based rule engine implementation
- Alternative simple rule engine implementation using Spring Expression Language (SpEL)
- Configuration-based rule engine selection
- Customer-specific rule management
- Transaction classification based on rules
- Performance optimizations including caching

## Getting Started

### Running the Application

1. Start the application using Maven:
   ```
   mvn spring-boot:run
   ```

2. Access the Swagger UI at:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

### Sample API Usage

To classify a transaction:

```
POST /api/transactions/classify?customerId=cust01

{
  "merchant": "Best Buy",
  "merchantType": "ELECTRONICS",
  "amount": 1200,
  "location": "Tokyo"
}
```

## Rule Engine Configuration

The application supports multiple rule engine implementations that can be configured in `application.properties`:

```properties
# Rule Engine Configuration
# Possible values: drools, simple
ruleengine.type=drools
```

### Available Rule Engines

1. **Drools Rule Engine** (default)
   - Uses the Drools rule engine
   - Supports complex rule conditions
   - High performance for large rule sets

2. **Simple Rule Engine**
   - Uses Spring Expression Language (SpEL)
   - Lighter weight alternative
   - Suitable for simpler rule conditions

## Architecture

The rule engine framework is designed around the following components:

- `RuleEngine` - Interface defining the contract for rule engine implementations
- `DroolsRuleEngine` - Implementation using Drools
- `SimpleRuleEngine` - Alternative implementation using SpEL
- `RuleEngineService` - Service layer that delegates to the configured rule engine
- `RuleEngineConfig` - Configuration for selecting the active rule engine

## Extending with New Rule Engines

To add a new rule engine implementation:

1. Create a new class that implements the `RuleEngine` interface
2. Register it as a Spring bean
3. Update the `RuleEngineConfig` to include your new implementation
4. Add your implementation type to the configuration options
