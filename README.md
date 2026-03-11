# Vouchera

## 1. Create the Database

The backend is configured to use:
- URL: `jdbc:postgresql://localhost:5432/vouchera`

Create the database:

```bash
psql postgres
```

Then in the `psql` prompt:

```sql
CREATE DATABASE vouchera;
\q
```

If your PostgreSQL requires credentials, set them before starting backend:

```bash
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
```

## 2. Run Backend

From project root:

```bash
cd backend
./mvnw spring-boot:run
```

Backend will run on `http://localhost:8080`

## 3. Run Frontend

Open a new terminal from project root:

```bash
cd frontend
npm install
npm run dev
```

Frontend will run on `http://localhost:5173`
