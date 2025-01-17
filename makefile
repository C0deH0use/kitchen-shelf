.PHONY: build run run-dependencies run-db destroy

build:
	docker compose build shelf_api

run: build
	docker compose up shelf_api

run-dependencies:
	docker compose --profile dependencies up --force-recreate --renew-anon-volumes

run-db:
	docker compose --profile db up --force-recreate --renew-anon-volumes

destroy:
	docker compose down -v
	docker container prune --force
