import csv
import random
from faker import Faker

fake = Faker()

NUMBER_OF_RECORDS = 100_000

with open("customers.csv", "w", newline="", encoding="utf-8") as file:

    writer = csv.writer(file)

    writer.writerow([
        "customerId",
        "firstName",
        "lastName",
        "email",
        "age",
        "city",
        "country",
        "registrationDate",
        "active"
    ])

    for i in range(NUMBER_OF_RECORDS):

        writer.writerow([
            f"C{i:06}",
            fake.first_name(),
            fake.last_name(),
            fake.email(),
            random.randint(18, 80),
            fake.city(),
            fake.country(),
            fake.date_between(start_date="-5y", end_date="today"),
            random.choice([True, False])
        ])

print("CSV generated.")