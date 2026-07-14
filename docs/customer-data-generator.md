# Customer Data Generator

The `generate_customer_data.py` script generates fake customer data using the `Faker` library in Python.

## Description

This script creates realistic customer records with the following fields:

- **customerId**
- **firstName**
- **lastName**
- **email**
- **age**
- **city**
- **country**
- **registrationDate**
- **active**

The total number of records is configurable through the `NUMBER_OF_RECORDS` constant in the script. By default, it generates **100,000 customer records**.

## Requirements

- Python 3.x

## Installation

1. **Install Python** (if not already installed):

    - Download the latest Python installer for Windows from https://www.python.org/downloads/windows/
    - Run the installer and **make sure to check "Add Python to PATH"**
    - Verify the installation by opening Command Prompt and running:

   ```sh
   python --version
   ```

2. **Install the Faker library**:

   ```sh
   pip install faker
   ```

## Usage

Generate the customer dataset by running:

```sh
python generate_customer_data.py
```

This will create a file named:

```text
customers.csv
```

After generating the file, copy or move it to the Spring Boot project's resources directory:

```text
src/
└── main/
    └── resources/
        └── input/
            └── customers.csv
```

The application reads this file from the classpath during execution.
