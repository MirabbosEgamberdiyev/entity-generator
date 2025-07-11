# Entity Generator

## Overview

Entity Generator is a Spring Boot application designed to automate the creation of database entities and related components (Entity, DTO, Repository, Service, and Controller) for Java-based applications. It provides a web-based interface for defining entity metadata and generating corresponding Java classes with proper annotations, including JPA, Swagger, and validation annotations. The application supports both single entity generation and batch processing, with features like file overwriting, validation, and preview generation.

The project leverages Spring Boot, JPA, JavaPoet for code generation, and Swagger for API documentation. It includes a user-friendly frontend built with HTML, CSS, and JavaScript to interact with the backend API.

## Features

- **Entity Generation**: Automatically generates Entity, DTO, Repository, Service, and Controller classes based on provided metadata.
- **Model Analysis**: Analyzes uploaded Java model files to extract metadata and generate components.
- **Validation**: Validates entity metadata before generation, ensuring correctness.
- **Batch Processing**: Supports generating multiple entities in a single request.
- **Preview Mode**: Allows previewing generated code without saving files.
- **Swagger Integration**: Provides API documentation via Swagger UI.
- **Frontend Interface**: A responsive web interface for defining entities, fields, and relationships.
- **File Management**: Delete generated files and list existing entities.
- **Customizable**: Supports various data types, validation rules, and relationship types (OneToOne, OneToMany, ManyToOne, ManyToMany).

## Prerequisites

- **Java**: 17 or later
- **Maven**: For dependency management and building the project
- **PostgreSQL**: Database for storing entity metadata (configurable in `application.properties`)
- **Node.js**: Optional, for local development of the frontend (if modifying static assets)
- **IDE**: IntelliJ IDEA, Eclipse, or any Java-compatible IDE

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd entity-generator
   ```

2. **Configure the Database**:
    - Ensure PostgreSQL is installed and running.
    - Create a database named `auto_generator_db`.
    - Update the `application.properties` file with your database credentials:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/auto_generator_db
      spring.datasource.username=postgres
      spring.datasource.password=your_password
      ```

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   The application will start on `http://localhost:8080`.

5. **Access the Application**:
    - **Web Interface**: Open `http://localhost:8080` in your browser to use the entity generation form.
    - **Swagger UI**: Access `http://localhost:8080/swagger-ui` for API documentation.

## Project Structure

```
entity-generator/
??? src/
?   ??? main/
?   ?   ??? java/
?   ?   ?   ??? com/example/entity_generator/
?   ?   ?       ??? EntityGenerator.java           # REST controller for API endpoints
?   ?   ?       ??? EntityGeneratorApplication.java # Spring Boot application entry point
?   ?   ?       ??? service/
?   ?   ?       ?   ??? EntityGeneratorService.java # Core logic for code generation
?   ?   ?       ?   ??? ModelAnalyzerService.java   # Analyzes Java models for metadata
?   ?   ?       ??? model/                         # Data models for metadata
?   ?   ??? resources/
?   ?       ??? static/
?   ?       ?   ??? index.html                     # Main frontend page
?   ?       ?   ??? styles.css                     # CSS styles for the frontend
?   ?       ?   ??? script.js                      # JavaScript for frontend logic
?   ?       ??? application.properties             # Application configuration
??? pom.xml                                        # Maven dependencies
??? README.md                                      # This file
```

## Usage

1. **Access the Web Interface**:
    - Navigate to `http://localhost:8080`.
    - Enter an entity name, add fields (with types, validations, etc.), and relationships as needed.
    - Click "Generate Entity" to create the components.

2. **API Usage**:
    - Use the `/api/generator/generate` endpoint to generate entities programmatically. Example:
      ```bash
      curl -X POST http://localhost:8080/api/generator/generate \
      -H "Content-Type: application/json" \
      -d '{"entityName":"User","fields":[{"name":"username","type":"String","nullable":false}]}'
      ```
    - Explore other endpoints via Swagger UI at `http://localhost:8080/swagger-ui`.

3. **Generated Files**:
    - Generated files are saved in `src/main/java/com/example/generated/` under subpackages (`entity`, `dto`, `repository`, `service`, `controller`).

## API Endpoints

| Endpoint                          | Method | Description                              |
|-----------------------------------|--------|------------------------------------------|
| `/api/generator/generate`          | POST   | Generate entity and components           |
| `/api/generator/generate-from-model` | POST   | Generate from uploaded Java model       |
| `/api/generator/validate`          | POST   | Validate metadata without generation     |
| `/api/generator/types`             | GET    | Get supported data types                 |
| `/api/generator/validation-rules`  | GET    | Get supported validation rules           |
| `/api/generator/relationship-types`| GET    | Get supported relationship types         |
| `/api/generator/generate-batch`    | POST   | Batch generate multiple entities         |
| `/api/generator/preview`           | GET    | Preview generated code                   |
| `/api/generator/generated/{entityName}` | DELETE | Delete generated files for an entity |
| `/api/generator/generated`         | GET    | List all generated entities              |

## Technologies Used

- **Backend**:
    - Spring Boot
    - Spring Data JPA
    - JavaPoet (for code generation)
    - Swagger (OpenAPI 3)
    - PostgreSQL
    - SLF4J (logging)
- **Frontend**:
    - HTML/CSS/JavaScript
    - Custom-built interface with vanilla JavaScript
- **Build Tool**: Maven

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License.

## Contact

For questions or feedback, please open an issue on the repository or contact the project maintainers.