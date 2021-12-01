# KotLink - Engineering Guide

### Required Software
* jdk 11+
* docker
* docker-compose
* Intellij IDEA (recommended)

### How To Develop Locally
* To run the server in Terminal use `./gradlew bootRun`
* To be able to run the server in IDE you will need to run `./gradlew composeUp` first
* Go to `http://localhost:8080` in your browser and create your namespaces / links
* Both Chrome and FireFox can be pointed at `browser-extension` directory 
to load the extension (or you can install it from the store)
* To run the test suit execute `./gradlew check`

### Best Practices
* Use Intellij IDEA to format the files you touched before committing them
* Strive for high test coverage, especially for backend code that is easy to unit-test
* Run `./gradlew detekt` to see if you have violated any of the code style checks
* Run the whole test suit locally before pushing your changes (e.g. with `./gradlew check`)
* Test UI manually as well, as the existing tests don't provide any guarantee that nothing isn't broken there