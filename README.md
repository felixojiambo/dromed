# Dromed

This is a Spring Boot application that manages drones for delivering medications. It includes features such as registering drones, loading medications onto drones, checking available drones, and monitoring battery levels.

## Table of Contents
- [Requirements](#requirements)
- [Setup](#setup)
- [Build and Run](#build-and-run)
- [Testing](#testing)
- [REST API Endpoints](#rest-api-endpoints)
  - [Register a Drone](#register-a-drone)
  - [Get Available Drones](#get-available-drones)
  - [Check Drone Battery Level](#check-drone-battery-level)
  - [Load Medication](#load-medication)
  - [Get Medications by Drone](#get-medications-by-drone)
  - [Load Drone with Medication](#load-drone-with-medication)
  - [Start Delivery](#start-delivery)
  - [Complete Delivery](#complete-delivery)
  - [Return to Base](#return-to-base)
  - [Battery Monitoring](#battery-monitoring)
- [Notes](#notes)

## Requirements

- Java 17 or later
- Maven 3.6.0 or later

## Setup

1. **Clone the repository:**
    ```sh
    git clone https://github.com/felixojiambo/dromed.git
    cd dromed
    ```

2. **Install dependencies:**
    ```sh
    mvn clean install
    ```

## Build and Run

1. **Build the application:**
    ```sh
    mvn clean package
    ```

2. **Run the application:**
    ```sh
    mvn spring-boot:run
    ```

The application will be accessible at `http://localhost:8080`.

## Testing

1. **Run unit tests:**
    ```sh
    mvn test
    ```


## REST API Endpoints

### Register a Drone

- **URL:** `/api/drones`
- **Method:** `POST`
- **Request Body:**
    ```json
    {
        "serialNumber": "SN123",
        "model": "LIGHTWEIGHT",
        "weightLimit": 200,
        "batteryCapacity": 80,
        "state": "IDLE"
    }
    ```
- **Response:**
    ```json
    {
        "id": 1,
        "serialNumber": "SN123",
        "model": "LIGHTWEIGHT",
        "weightLimit": 200,
        "batteryCapacity": 80,
        "state": "IDLE"
    }
    ```
- **Purpose:** Registers a new drone in the system with the specified details.

### Get Available Drones

- **URL:** `/api/drones/available`
- **Method:** `GET`
- **Response:**
    ```json
    [
        {
            "id": 1,
            "serialNumber": "SN123",
            "model": "LIGHTWEIGHT",
            "weightLimit": 200,
            "batteryCapacity": 80,
            "state": "IDLE"
        },
        {
            "id": 2,
            "serialNumber": "SN124",
            "model": "MIDDLEWEIGHT",
            "weightLimit": 300,
            "batteryCapacity": 60,
            "state": "IDLE"
        }
    ]
    ```
- **Purpose:** Retrieves a list of all drones that are currently available for use.

### Check Drone Battery Level

- **URL:** `/api/drones/{id}/battery`
- **Method:** `GET`
- **Response:**
    ```json
    {
        "batteryCapacity": 80
    }
    ```
- **Purpose:** Checks and returns the battery level of the specified drone.

### Load Medication

- **URL:** `/api/medications`
- **Method:** `POST`
- **Request Body:**
    ```json
    {
        "name": "Paracetamol",
        "weight": 50,
        "code": "PARA_001",
        "imageUrl": "http://example.com/images/paracetamol.jpg"
    }
    ```
- **Response:**
    ```json
    {
        "id": 1,
        "name": "Paracetamol",
        "weight": 50,
        "code": "PARA_001",
        "imageUrl": "http://example.com/images/paracetamol.jpg"
    }
    ```
- **Purpose:** Adds a new medication to the system with the specified details.

### Get Medications by Drone

- **URL:** `/api/drones/{id}/medications`
- **Method:** `GET`
- **Response:**
    ```json
    [
        {
            "id": 1,
            "name": "Paracetamol",
            "weight": 50,
            "code": "PARA_001",
            "imageUrl": "http://example.com/images/paracetamol.jpg"
        },
        {
            "id": 2,
            "name": "Aspirin",
            "weight": 30,
            "code": "ASPI_002",
            "imageUrl": "http://example.com/images/aspirin.jpg"
        }
    ]
    ```
- **Purpose:** Retrieves a list of medications loaded onto the specified drone.

### Load Drone with Medication

- **URL:** `/api/drones/load`
- **Method:** `POST`
- **Request Parameters:**
    - `droneId` (as query parameter)
- **Request Body:**
    ```json
    {
        "name": "Paracetamol",
        "weight": 50,
        "code": "PARA_001",
        "imageUrl": "http://example.com/images/paracetamol.jpg"
    }
    ```
- **Response:**
    ```json
    {
        "id": 1,
        "drone": {
            "id": 1,
            "serialNumber": "SN123",
            "model": "LIGHTWEIGHT",
            "weightLimit": 200,
            "batteryCapacity": 80,
            "state": "IDLE"
        },
        "medication": {
            "id": 1,
            "name": "Paracetamol",
            "weight": 50,
            "code": "PARA_001",
            "imageUrl": "http://example.com/images/paracetamol.jpg"
        }
    }
    ```
- **Purpose:** Loads the specified medication onto the specified drone.

### Start Delivery

- **URL:** `/api/drones/{id}/start-delivery`
- **Method:** `POST`
- **Response:** `200 OK`
- **Purpose:** Marks the specified drone as starting a delivery.

### Complete Delivery

- **URL:** `/api/drones/{id}/complete-delivery`
- **Method:** `POST`
- **Response:** `200 OK`
- **Purpose:** Marks the specified drone as having completed its delivery.

### Return to Base

- **URL:** `/api/drones/{id}/return-to-base`
- **Method:** `POST`
- **Response:** `200 OK`
- **Purpose:** Marks the specified drone as returning to the base after completing its tasks.

### Mark Drone as Idle

- **URL:** `/api/drones/{id}/mark-idle`
- **Method:** `POST`
- **Response:** `200 OK`
- **Purpose:** Marks the specified drone as idle after it has returned to base.

### Battery Monitoring

- **Battery Check Service:** The application includes a battery monitoring service that periodically checks the battery levels of all drones.
- **Logging:** Battery levels are logged to help monitor battery usage and identify when drones need recharging.
- **Purpose:** Ensures that the drones' battery levels are constantly monitored and logged for optimal operation and maintenance.

## Notes

- Ensure your environment meets the required dependencies.
- The application uses an in-memory H2 database. All data will be lost when the application stops.
- For production, configure a persistent database and update the `application.properties` file accordingly.
