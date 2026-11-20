.PHONY: build-docs lint

build-docs:
	bash ./scripts/build-docs.sh

lint:
	bash ./scripts/lint.sh

.PHONY: up down test-api rate-limit pact
up:
	docker compose up -d --build

down:
	docker compose down -v

test-api:
	bash tests/test-api.sh

rate-limit:
	bash tests/rate-limit-demo.sh

pact:
	mvn -B -pl src/cart-service test
	mvn -B -pl src/catalog-service test
