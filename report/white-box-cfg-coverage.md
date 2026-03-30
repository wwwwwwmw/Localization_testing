# White-Box CFG & Coverage Report

## Coverage Summary

| Validator Type | Statement Coverage | Branch Coverage |
|---|---:|---:|
| Currency | 100.0% (4/4) | 100.0% (4/4) |
| Untranslated Text | 100.0% (4/4) | 100.0% (4/4) |
| Date Format | 100.0% (4/4) | 100.0% (4/4) |
| Layout Direction | 100.0% (4/4) | 100.0% (4/4) |
| Text Overflow | 100.0% (3/3) | 100.0% (3/3) |
| Charset | 100.0% (4/4) | 100.0% (4/4) |
| Number & Measurement | 100.0% (4/4) | 100.0% (4/4) |
| Media & Alt | 100.0% (4/4) | 100.0% (4/4) |
| URL Routing | 100.0% (3/3) | 100.0% (3/3) |

## CFG Diagrams (Mermaid)

### Currency

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Untranslated Text

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Date Format

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Layout Direction

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Text Overflow

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Charset

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Number & Measurement

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### Media & Alt

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

### URL Routing

```mermaid
flowchart TD
    A[Start] --> B[Collect Inputs]
    B --> C{Data Available?}
    C -->|No| D[Create Error]
    C -->|Yes| E[Apply Validation Rules]
    E --> F{Rule Violations?}
    F -->|Yes| D
    F -->|No| G[Pass]
    D --> H[Return errors]
    G --> I[Return empty list]
```

