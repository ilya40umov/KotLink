# KotLink - Engineering Guide

### Required Software
* jdk 8+
* direnv
* docker
* docker-compose
* Intellij IDEA (recommended)

### How To Develop Locally
* First of all, `cd` into the project directory and run `direnv allow`
* Then, start dependencies with `kotlink_env_up`
* Run application in Terminal with `./gradlew bootRun` (or in Intellij IDEA)
* Go to `http://localhost:8080` in your browser and create your namespaces / links
* Both Chrome and FireFox can be pointed at `browser-extension` directory 
to load the extension (or you can install it from the store)
* Stop dependencies with `kotlink_env_down` (this command will also remove all data from Postgres)
* You can run the CI pipeline with `kotlink_ci` (can be run in parallel with the app)

### TODOs

* finish writing tests for AliasService
* refine how security is configured
* package the backend as a docker image and write deployment instructions
* add instructions on security (oauth and extension secrets)