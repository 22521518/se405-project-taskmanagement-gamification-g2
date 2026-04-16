# Navigation Guide

This project uses Compose Type-Safe Navigation.

## 1. Defining Routes
Routes are defined as `Serializable` classes or objects. We centralize route definitions in `NavigationNames.kt`.

### Navigation without Parameters
Use an `object` for screens that do not require any parameters.
```kotlin
import kotlinx.serialization.Serializable

@Serializable
object TaskManagementNav
```

### Navigation with Parameters
Use a `data class` for screens that require parameters to be passed.
```kotlin
@Serializable
data class TaskDetailNav(
    val taskId: String,
    val isEditMode: Boolean
)
```

## 2. Integrating in MainNavHost
Declare the routes inside `MainNavHost.kt` using `composable<YourRoute>`.

### Registering and Accessing Parameters
The `navBackStackEntry` gives you type-safe access to the parameters using `toRoute<YourRoute>()`.

```kotlin
NavHost(
    navController = navController,
    startDestination = TaskManagementNav
) {
    // 1. Registering Screen Without Params
    composable<TaskManagementNav> { 
        TaskManagementScreen() 
    }

    // 2. Registering Screen With Params & Extracting Params
    composable<TaskDetailNav> { navBackStackEntry ->
        // Type-safe deserialization of arguments
        val args = navBackStackEntry.toRoute<TaskDetailNav>()
        
        // Pass to your screen logic
        TaskDetailScreen(
            taskId = args.taskId,
            isEditMode = args.isEditMode
        )
    }
}
```

## 3. How to Trigger Navigation
Because of the type-safe nature, you can navigate using the exact classes instead of raw strings.

```kotlin
val navController = LocalNavController.current

// Navigate without params
navController.navigate(TaskManagementNav)

// Navigate with params
navController.navigate(TaskDetailNav(taskId = "uuid-1234", isEditMode = true))

// Go Back
navController.popBackStack()
```
