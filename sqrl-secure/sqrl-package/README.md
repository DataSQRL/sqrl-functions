# Secure Functions

The `datasqrl.secure` package provides utilities for generating random identifiers, including IDs and UUIDs.
These functions are useful for creating unique keys, random values, or other identifiers in your SQRL scripts.

## Functions Overview

| **Function Name**         | **Description**                                               |
|---------------------------|---------------------------------------------------------------|
| **RandomID(bigint)**      | Generates a random ID of the specified number of bytes.       |
|                           | Example: `RandomID(16) → '3wJq7dJkQh5HztHWXcQeXQ'`            |
| **Uuid()**                | Generates a random UUID in the standard 36-character format.  |
|                           | Example: `Uuid() → '550e8400-e29b-41d4-a716-446655440000'`    |

## SQRL Script Example

You can use these functions in a SQRL script like this:
```sql
IMPORT datasqrl.random.*;

random_ids := SELECT 
    RandomID(16) AS random_id,
    Uuid() AS uuid
FROM your_table;

-- Displaying the result
SELECT * FROM random_ids;
```