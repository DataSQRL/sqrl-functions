# SQRL Functions
Function Package Implementations for DataSQRL

This guide explains how to upload a function package to the DataSQRL Repository.

---

## Uploading a Function Package

Follow these steps to upload or update a function package in the DataSQRL repository:

### 1. Log In to the SQRL CLI
Use the SQRL CLI app to authenticate yourself:  
```bash
sqrl login
```

### 2. Prepare the Function Package
Each function package must include a `sqrl-package` directory. Navigate to this directory or create it if it doesn’t exist.
Then, copy the necessary assemblies, such as *.jar files, into this folder.

### 3. Update Function Descriptors
Function descriptors are used to tell the compiler how to load the functions. Make the following updates as needed:

1. If adding a new function to the package, create the corresponding function descriptor. For guidance, refer to the [official documentation](http://www.datasqrl.com/docs/reference/sqrl/functions/custom-functions/#create-function-descriptors).
2. If an existing function has been updated, ensure the version is updated in the corresponding function descriptor file.

### 4. Update the Package Version
Increment the version in the `package.json` file to reflect the new changes.

### 5. Publish the Package
Publish the updated package to the DataSQRL repository:  
```bash
sqrl publish
```

---

By following these steps, you ensure that your function package is properly updated and available for use in the DataSQRL Repository.  