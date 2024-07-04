# Dromed

This is a Spring Boot application that manages drones for delivering medications. It includes features such as registering drones, loading medications onto drones, checking available drones, and monitoring battery levels.

## Table of Contents
- [Requirements](#requirements)
- [Setup](#setup)
- [Build and Run](#build-and-run)
- [Testing](#testing)
- [REST API Endpoints](#rest-api-endpoints)
  - [Register a Drone](#register-a-drone)
  - [Update Drone State](#update-drone-state)
  - [Load Drone with Medication](#load-drone-with-medication)
  - [Get Available Drones](#get-available-drones)
  - [Check Drone Battery Level](#check-drone-battery-level)
  - [Get Medications by Drone](#get-medications-by-drone)
  - [Start or Complete Delivery](#start-or-complete-delivery)
  - [Return to Base](#return-to-base)
- [Notes](#notes)

## Requirements

- Java 21 or later
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

- **URL:** `/api/v1/drones`
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
        "success": true,
        "message": "Drone registered successfully",
        "data": {
            "serialNumber": "SN123",
            "model": "LIGHTWEIGHT",
            "weightLimit": 200,
            "batteryCapacity": 80,
            "state": "IDLE"
        }
    }
    ```
- **Purpose:** Registers a new drone in the system with the specified details.

### Update Drone State

- **URL:** `/api/v1/drones/{id}/state`
- **Method:** `PATCH`
- **Request Body:**
    ```json
    {
        "state": "IDLE"
    }
    ```
- **Response:**
    ```json
    {
        "success": true,
        "message": "Drone state updated successfully"
    }
    ```
- **Purpose:** Updates the state of a drone (e.g., from `LOADING` to `IDLE`).

### Load Drone with Medication

- **URL:** `/api/v1/drones/{id}/medications`
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
        "success": true,
        "message": "Medication loaded successfully",
        "data": {
            "drone": {
                "id": 1,
                "serialNumber": "SN123",
                "model": "LIGHTWEIGHT",
                "weightLimit": 200,
                "batteryCapacity": 80,
                "state": "LOADING"
            },
            "medication": {
                "id": 1,
                "name": "Paracetamol",
                "weight": 50,
                "code": "PARA_001",
                "imageUrl": "http://example.com/images/paracetamol.jpg"
            }
        }
    }
    ```
- **Purpose:** Loads the specified medication onto the specified drone.

### Get Available Drones

- **URL:** `/api/v1/drones`
- **Method:** `GET`
- **Request Parameters:**
    - `state` (optional): Filter drones by state (e.g., `IDLE`, `LOADING`)
- **Response:**
    ```json
    {
        "success": true,
        "message": "List of available drones",
        "data": [
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
    }
    ```
- **Purpose:** Retrieves a list of all drones that are currently available for use.

### Check Drone Battery Level

- **URL:** `/api/v1/drones/{droneId}/battery-level`
- **Method:** `GET`
- **Response:**
    ```json
    {
        "success": true,
        "message": "Battery level retrieved successfully",
        "data": 80
    }
    ```
- **Purpose:** Checks and returns the battery level of the specified drone.

### Get Medications by Drone

- **URL:** `/api/v1/drones/{droneId}/medications`
- **Method:** `GET`
- **Response:**
    ```json
    {
        "success": true,
        "message": "List of medications retrieved successfully",
        "data": [
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
    }
    ```
- **Purpose:** Retrieves a list of medications loaded onto the specified drone.

### Start or Complete Delivery

- **URL:** `/api/v1/drones/{droneId}/deliveries`
- **Method:** `POST`
- **Request Parameters:**
    - `action` (required): Either `start` or `complete`
- **Response:**
    ```json
    {
        "success": true,
        "message": "Delivery process started"
    }
    ```
    or
    ```json
    {
        "success": true,
        "message": "Delivery completed successfully"
    }
    ```
- **Purpose:** Marks the specified drone as starting or completing a delivery.

### Return to Base

- **URL:** `/api/v1/drones/{droneId}/return-to-base`
- **Method:** `POST`
- **Response:**
    ```json
    {
        "success": true,
        "message": "Drone return process started"
    }
    ```
- **Purpose:** Marks the specified drone as returning to the base after completing its tasks.

## Notes

- Ensure your environment meets the required dependencies.
- The application uses an in-memory H2 database. All data will be lost when the application stops.
- For production, configure a persistent database and update the `application.properties` file accordingly.
