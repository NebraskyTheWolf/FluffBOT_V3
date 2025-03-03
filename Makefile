GIT_SHA_FETCH := $(shell git rev-parse HEAD)
export GIT_SHA=$(GIT_SHA_FETCH)

.PHONY: all build_jar build-mysql build_docker

all: build_jar build-mysql build_docker

furraiddb: build_jar build_furraiddb

build_jar:
	chmod +x ./gradlew
	./gradlew clean shadowJar

build_jar_win:
	gradlew clean shadowJar

build_docker:
	docker build --build-arg GIT_SHA=$(GIT_SHA_FETCH) . -t ghcr.io/fluffici/fluffbot-beta:latest
	docker push ghcr.io/fluffici/fluffbot-beta:latest

build_furraiddb:
	docker build --build-arg GIT_SHA=$(GIT_SHA_FETCH) . -f F:\FluffBOT_V3\Dockerfile.furraiddb -t ghcr.io/fluffici/furraiddb-bot-beta:latest
	docker push ghcr.io/fluffici/furraiddb-bot-beta:latest

build-mysql:
	docker build --build-arg GIT_SHA=$(GIT_SHA_FETCH) ./docker/mysql -t ghcr.io/fluffici/mysql-fluffici:latest
	docker push ghcr.io/fluffici/mysql-fluffici:latest
