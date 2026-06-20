# SkillMatch

SkillMatch is an intelligent recruitment and skills-matching platform. It features a robust backend built with Java (Spring Boot) and an AI/NLP processing service powered by Python to extract, parse, and score resumes (CVs) dynamically.

## 📌 Project Architecture

The project is managed as a monorepo containing two main modules:

- **`web-app/`** — The web application and core business logic handler (Spring Boot + Maven + Thymeleaf).
- **`ai-module/`** — The artificial intelligence microservice responsible for document text extraction (`data_extraction.py`) and LLM orchestration.

---

## 🛠️ Prerequisites

Before running the application, ensure you have the following installed:

- **Java JDK 17**
- **Maven 3.x**
- **Python 3.10+**
- A relational database: MySQL

---

## 🚀 Getting Started

Follow these instructions to set up and run both modules on your local machine.

### 1. AI Module Setup (`ai-module`)

The AI module uses Python to extract text from candidate CVs and process it through a Large Language Model.

**Navigate to the AI directory:**

```bash
cd ai-module
```

**Create and activate an isolated Python virtual environment:**

Windows:
```bash
python -m venv .venv
.venv\Scripts\activate
```

macOS/Linux:
```bash
python3 -m venv .venv
source .venv/bin/activate
```

**Install all required dependencies:**

```bash
pip install -r requirements.txt
```
Or, if you use uv:

```bash
uv pip install -r requirements.txt
```

**Environment Variables Configuration:**

Create a `.env` file inside the `ai-module/` directory to store your sensitive credentials:

```
API_KEY=your_actual_ollama_api_key_here
```

---

### 2. Web Application Setup (`web-app`)

The Spring Boot backend handles user registration, secure CV uploading, and dynamically calls the Python script via a subprocess to process documents.

**Navigate to the web application directory:**

```bash
cd web-app
```

**Database Configuration:**

Open `src/main/resources/application.properties` and update the database configuration to match your local instance:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/skillmatch_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

**Verify OS-Specific AI Paths:**

In the same `application.properties` file, verify that the path to your Python virtual environment executable matches your operating system. By default, it is configured for Windows relative to the `web-app/` folder:

Windows:
```properties
ai.python.path=../ai-module/.venv/Scripts/python.exe
ai.script.path=../ai-module/main.py
```

macOS/Linux (uncomment/modify if running on Unix systems):
```properties
ai.python.path=../ai-module/.venv/bin/python
ai.script.path=../ai-module/main.py
```

**Build the project and download Maven dependencies:**

```bash
mvn clean install
```

**Run the Spring Boot application:**

```bash
mvn spring-boot:run
```

The web interface will be accessible at: **http://localhost:8080**

---

## 🔒 Security & Best Practices

Never commit active API credentials or database passwords. The root `.gitignore` file is pre-configured to block `.env` and local build outputs from being tracked by Git.