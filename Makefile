PHONY: up down ci dep-updates-check
DEFAULT_TARGET: build

up:
	./environment/bin/kotlink_env_up

down:
	./environment/bin/kotlink_env_down

ci:
	./environment/bin/kotlink_ci

dep-updates-check:
	./gradlew dependencyUpdates
