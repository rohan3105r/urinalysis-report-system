# Urinalysis Report Management and Decision-Support System

A student full-stack prototype built with React, Spring Boot, and MongoDB. It stores sample test records, applies simple configurable demonstration rules to flag results for review, and shows report history.

> Educational demo only. This is not a medical diagnostic tool. The example thresholds and flags are illustrative and must not be used for real patient care.

## Requirements
- Java 17+
- Node.js 20+
- MongoDB Community Server running locally on `mongodb://localhost:27017`
- Maven (or use the included Maven Wrapper if added separately; this starter uses Maven installed on your machine)

## 1. Run the backend
Open a terminal in `backend` and run:
```bash
mvn spring-boot:run
```
API base URL: `http://localhost:8080/api/tests`

If Maven is not installed, open the `backend/pom.xml` project in IntelliJ IDEA and run `UrinalysisApplication`, or install Maven.

## 2. Run the frontend
Open a second terminal in `frontend`:
```bash
npm install
npm run dev
```
Open the URL shown by Vite (normally `http://localhost:5173`).

## Features
- Create, read, update, and delete test records
- Dashboard counts and flagged-result list
- Demonstration rule engine for result flagging
- Searchable report history
- Frontend-to-backend REST integration
- MongoDB persistence

## API endpoints
- `GET /api/tests`
- `GET /api/tests/{id}`
- `POST /api/tests`
- `PUT /api/tests/{id}`
- `DELETE /api/tests/{id}`
- `GET /api/tests/stats`

## Example test JSON
```json
{
  "sampleId": "SAMPLE-1001",
  "testDate": "2026-10-29",
  "ph": "6.0",
  "specificGravity": "1.020",
  "glucose": "Negative",
  "protein": "Negative",
  "ketones": "Negative",
  "blood": "Negative",
  "nitrite": "Negative",
  "leukocytes": "Negative",
  "notes": "Demo record"
}
```

## Rule-engine note
This prototype uses illustrative ranges/positive-value flags to demonstrate a software workflow. Real interpretation depends on laboratory methods, validated reference ranges, clinical context, and qualified professionals. Do not enter identifiable patient data.
