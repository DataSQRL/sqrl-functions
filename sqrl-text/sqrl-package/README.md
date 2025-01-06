# String Formatting Functions

The `datasqrl.text` package provides string manipulation functions, including formatting strings with dynamic arguments.

## Functions Overview

| **Function Name**                         | **Description**                                                                                                                                                                            |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **format(String text, [String arguments...])** | Formats a string using the specified arguments. Optional parameters include `arguments` that will be inserted into the format string at corresponding placeholders.                          |
|                                           | Example: \`format('Hello %s', 'World') → 'Hello World'\`                                                                                                                                   |

## SQRL Script Example

You can use these functions in a SQRL script like this:
```sql
IMPORT datasqrl.text.*;

result_table := SELECT format('Hello %s!', 'SQRL') AS formatted_greeting
FROM your_table;

-- Displaying the result
SELECT * FROM result_table;
