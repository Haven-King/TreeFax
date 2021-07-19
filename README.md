# TreeFax ![latest version](https://img.shields.io/github/v/release/Haven-King/TreeFax)

A library that keeps track of trees that spawn in the world. TreeFax has a fairly
small amount of code and logic that it does, but because it stores a relatively
large amount of data, it's better for multiple mods doing similar things to all
share one source of truth, rather than bloat world sizes.

### Usage
You can add it to your build.gradle with the following snippets:
```groovy
maven {
    url = 'https://hephaestus.dev/release'
}
```

```groovy
dependencies {
    // It is highly recommended to just include TreeFax in your built jar
    modImplementation(include("dev.hephaestus:TreeFax:${project.treefax_version}"))
}
```

The [`TreeTracker`]() class will let you add, remove, and query the trees in
the world.