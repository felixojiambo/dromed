Here is an updated `README.md` for the Dromed project, including the battery monitoring feature.

---

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

//To be implemented 2. **Run integration tests:**
   
    ```sh
    mvn verify
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

### Check Drone Battery Level

- **URL:** `/api/drones/{id}/battery`
- **Method:** `GET`
- **Response:**
    ```json
    80
    ```

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

### Get Medications by Drone

- **URL:** `/api/medications/drone/{droneId}`
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

### Load Drone with Medication

- **URL:** `/api/drone-medications/load`
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

### Battery Monitoring

- **Battery Check Service:** The application includes a battery monitoring service that periodically checks the battery levels of all drones.
- **Logging:** Battery levels are logged to help monitor battery usage and identify when drones need recharging.

## Notes

- Ensure your environment meets the required dependencies.
- The application uses an in-memory H2 database. All data will be lost when the application stops.
- For production, configure a persistent database and update the `application.properties` file accordingly.
