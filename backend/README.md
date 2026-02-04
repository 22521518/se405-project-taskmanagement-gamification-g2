# Backend Project Structure and Code Conventions

## Project Structure

The backend project is organized as follows:

```
src/
└── main/
    ├── kotlin/
    │   └── com/
    │       └── example/
    │           └── se405/
    │               └── backend/
    │                   ├── controllers/
    │                   │   └── <domain-specific-controllers>.kt
    │                   ├── services/
    │                   │   └── <domain-specific-services>/
    │                   │       ├── GetService.kt
    │                   │       ├── UpdateService.kt
    │                   │       └── DeleteService.kt
    │                   ├── database/
    │                   │   ├── model/
    │                   │   │   └── <data-classes>.kt
    │                   │   ├── repository/
    │                   │   │   └── <repository-interfaces>.kt
    │                   │   └── repositoryImpl/
    │                   │       └── <repository-implementations>.kt
    │                   └── BackendApplication.kt
    └── resources/
        └── application.properties
```

### Folder Descriptions

- **`controllers/`**: Contains controllers organized by domain. Each controller handles routing and delegates logic to services.
- **`services/`**: Contains services organized by domain. Each domain has subfolders for specific actions (e.g., `GetService`, `UpdateService`, `DeleteService`).
- **`database/`**:
  - **`model/`**: Contains `data class` definitions representing database tables.
  - **`repository/`**: Contains interfaces defining database query methods.
  - **`repositoryImpl/`**: Contains implementations of the repository interfaces.

---

## Code Conventions

### General Naming Conventions

- **Classes**: Use `PascalCase` (e.g., `UserController`, `GetUserService`).
- **Methods and Variables**: Use `camelCase` (e.g., `getUserById`, `userRepository`).

### Example Implementations

#### Data Class in `model`

```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String
)
```

#### Repository Interface in `repository`

```kotlin
interface UserRepository {
    fun findById(id: Long): User?
    fun findAll(): List<User>
}
```

#### Repository Implementation in `repositoryImpl`

```kotlin
class UserRepositoryImpl : UserRepository {
    override fun findById(id: Long): User? {
        // Implementation logic
    }

    override fun findAll(): List<User> {
        // Implementation logic
    }
}
```

#### Service in `services`

```kotlin
class GetUserService(private val userRepository: UserRepository) {
    fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }
}
```

#### Controller in `controllers`

```kotlin
@RestController
@RequestMapping("/users")
class UserController(private val getUserService: GetUserService) {

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = getUserService.getUserById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
```
