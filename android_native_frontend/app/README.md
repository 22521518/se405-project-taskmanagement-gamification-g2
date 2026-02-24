# Project Structure Guide: Isolation, Layers & Testing

This document defines the **Vertical Slicing** architecture. The system is divided into **Epics** (feature-specific code) and **Core** (shared global code).

---

## 🏗 Core Folder (Shared Resources)

The `src/core/` directory contains code that is used by multiple features. This is where you put "Common" logic to avoid duplication across different team members' folders.

### 1. `core/presentation/` (Shared UI)
- **`theme/`**: The "Skin" of the app.
  - *Example*: `Color.kt`, `Type.kt`, `Theme.kt`, `Dimension.kt`
- **`components/`**: Standard UI elements used everywhere.
  - *Example*: `PrimaryButton.kt`, `LoadingSpinner.kt`, `ErrorMessage.kt`, `StandardTextField.kt`

### 2. `core/data/` (Shared Data Logic)
- **`network/`**: Global API client configuration.
  - *Example*: `NetworkClient.kt`, `BaseApiConfig.kt`
- **`database/`**: Global database configuration (if shared across epics).
  - *Example*: `SharedDatabaseProvider.kt`

### 3. `core/util/` (Shared Helpers)
- **`extensions/`**: Language extensions used app-wide.
  - *Example*: `DateExt.kt`, `StringExt.kt`
- **`constants/`**: Global constants.
  - *Example*: `AppConstants.kt`, `EndpointConstants.kt`

---

## 🏗 Detailed Layer Responsibilities (Within an Epic)

Inside `features/your_feature/`, each subfolder handles feature-specific logic:

### 1. `domain/` (Business Rules)
- **`entity/`**: Pure data entities (e.g., `Note.kt`).
- **`repository/`**: Interfaces defining data contracts (e.g., `NoteRepository.kt`).
- **`use_case/`**: Individual business actions (e.g., `AddNote.kt`).

### 2. `data/` (Infrastructure - REST/GraphQL)
- **`remote/`**: Network interaction. Contains API Service interfaces and Data Transfer Objects (DTOs).
  - *Example*: `UserApiService.kt`, `UserResponseDto.kt` (REST) or `User.graphql` (GraphQL).
- **`local/`**: (Optional) Persistence logic if caching is needed.
  - *Example*: `UserDao.kt`, `UserEntity.kt`.
- **`repository/`**: Concrete implementations of domain interfaces. It manages the data flow logic (e.g., fetch from network, then save to local).
  - *Example*: `UserRepositoryImpl.kt`.
- **`mapper/`**: Essential translation logic from Remote/Local models to clean Domain models.
  - *Example*: `UserMapper.kt`.

### 3. `presentation/` (User Interface)
- **`ui/`**: Screen layouts consuming `core/components`.
- **`state/`**: Logic managing UI state (e.g., `NoteViewModel.kt`).
- **`events/`**: User actions (e.g., `NoteEvent.kt`).

---

## 📂 Complete Visual Directory Tree

```text
src/
├── core/                      # 🌍 SHARED (Used by everyone)
│   ├── presentation/
│   │   ├── theme/             # Styling/Colors
│   │   └── components/        # Reusable Buttons/Inputs
│   ├── data/
│   │   └── network/           # Shared API Client
│   └── util/                  # Shared Helpers
│
├── features/                     # 👤 ISOLATED (Owned by individual members)
│   └── your_feature_epic/     
│       ├── domain/            # Logic (Entities, Repositories, UseCases)
│       ├── data/              # Infra (Remote API, Local DB, RepoImpl, Mapper)
│       │   ├── remote/        # -> UserApiService.kt, UserDto.kt
│       │   ├── local/         # -> UserDao.kt
│       │   ├── repository/    # -> UserRepositoryImpl.kt
│       │   └── mapper/        # -> UserMapper.kt
│       ├── presentation/      # UI (Screens, ViewModels, Events)
│
└── di/      # ⚙️ DEPENDENCY INJECTION dependency_injection
```

---

## 🤝 Rules for Cross-Feature Usage
1. **Epic/Feature -> Core**: ALWAYS allowed. Use shared components to keep the app looking consistent.
2. **Epic/Feature -> Epic/Feature**: ONLY via Domain Repository Interfaces. Never import a Scree**n** or a Database DAO from another member's Epic/Feature.
3. **Core -> Epic/Feature**: STRICTLY FORBIDDEN. The Core should never know about specific features to avoid circular dependencies.
