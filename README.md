# ANAV

Anav, A gradle plugin to help you start activities without `.class`. 

### Features:

* It's Just a plugin, no reflection, no runtime stuff (If you don't use the utility library), no initializations.
* Setter and getter for extras / arguments.
* Generate Kotlin codes for kapt and Java codes for apt.
* Supports both single-module projects and multi-module projects.

# Intro

Activities in multi-module projects are usually not able to access each other. That's to say, we can't create intent and start an Activity like this:

```kotlin
Intent(this, SettingsActivity::class.java).apply {
    startActivity(this)
}
```

But we can use the `Intent.setClassName(context, className)` do the same thing:

```kotlin
Intent(Intent.ACTION_VIEW).let {
    it.setClassName(this, "x.y.z.SettingsActivity")
    startActivity(this)
}
```

So, the job of Anav is processing all your Activities which are annotated by `@Navigable`:

```kotlin
// module A
@Navigable
class MainActivity : Activity()

// module B
@Navigable
class SettingsActivity : Activity()
```

And generate A 'navigation map' for you:

```kotlin
object NavMap {
   const val Main: String = "x.y.z.a.MainActivity"
   const val Settings: String = "x.y.z.b.SettingsActivity"
}
```

Then, remove hardcode strings from your navigation code:

```kotlin
Intent(Intent.ACTION_VIEW).let {
    it.setClassName(this, NavMap.Settings)
    startActivity(this)
}

// Or use functions from anav utils library
navigate(NavMap.Settings)
```

# Installation

1. In your root `build.gradle`:

```groovy
buildscript {
   // ...
   dependencies {
       classpath ''
   }
}
```

2. Apply the plugin in root `build.gradle`, or put it in app's `build.gradle` if you just want it to work for a single module project:

```groovy
apply plugin: 'com.dokar.anav'
```

**IMPORTANT: Set `baseModule` for multi-module using:**

```groovy
nanvConfig {
    // your base/shared/core module here
    baseModule = 'base'
}
```

3. Add dependencies in your modules:

```groovy
dependencies {
    implementation 'io.github.dokar3:anav-annotations:0.0.1'
    kapt 'io.github.dokar3:anav-compiler:0.0.1'
    // Optional
    implementation 'io.github.dokar3:anav-utils:0.0.1'
}
```

If you are not using Kotlin, replace `kapt` with `annotationProcessor`.

# More usage

### Extras / arguments

If you want to set some extras, Anav also provide some options for you:

```kotlin
@Navigable(
    args = ["userId","username"],
    argTypes = [Int::class, String::class] 
)
class ProfileActivity : Activity()
```

Anav will generate some extension properties for you:

```kotlin
class NavArgs {
    class Profile {
        // ...
        var Intent.userId: Int?
            get() = getIntExtra(USER_ID, 0)
            set(value) {
                putExtra(USER_ID, valu)
            }
        
        var Intent.username: String?
            get() = getStringExtra(USERNAME)
            set(value) {
                putExtra(USERNAME, value)
            }
    }
}
```

Then set and get them in your code:

```kotlin
navigate(NavMap.Profile) {
    it.userId = 3212
    it.username = "..."
}

// onCreate() of ProfileActivity
val userId: Int? = intent.userId
val userName: String? = intent.username
```

### Grouping

By default, Anav generates all fields in the top-level class, if you need to group them, this is the way:

```kotlin
@Navigable(group = "home")
class FeedsActivity : Activity()

@Navigable(group = "settings")
class AccountSettings : Activity()
```

They are in inner classes now:

```kotlin
object NavMap {
    object Home {
        // ...
        const val Feeds: String = "x.y.z.FeedsActivity"
    }
    object Settings {
        // ...
        const val Account: String = "x.y.z.AccountSettings"
    }
}
```

### Configs

There are some options to configure generated source code, in your root `build.gradle` file:

```groovy
anavConfig {
    // your configuration here
}
```

Options:

* `debug`: Boolean, true to enable log, default is `false`
* **`baseModule`**: String, set the base module for multi-module projects. All codes will be generated in base module, so your other modules can access these fields/methods.
* `packageName`: String, package name of generated source files, default is `${MODULE_PACKAGE_NAME}.navigation`. If the plugin is not successfully applied, the package name will be `anav`.
* `navMapClassName`: String, name of navigation fields class, default is `NavMap`.
* `navArgsClassName`: String, name of navigation arguments class, default is `NavArgs`.
* `removeActivitySuffix`: Boolean, remove the Activity suffix for field names. eg. `MainActivity` will be changed to `Main`, default is `true`. 

### Output
There is a difference between single-module projects and multi-module projects, the generated directory of source files in single-module projects will be:

`app/build/generated/${kapt or apt generated dir}`

And the directory in multi-module projects will be:

`base/main/java/`

You may want to generate source files into the main source directory in single-module projects, configure `baseModule`  in your app module:

```groovy
anavConfig {
    // same as your app module
    baseModule = 'app'
}
```

# Known issues

* Need to `make project` after your annotations were added, changed, or removed. And these may cause some `Unresolved reference` errors when it's finished. It's usually OK if codes are successfully generated, clean and make the project again, errors will gone.