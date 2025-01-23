.PHONY: build run run-kafka run-monitoring run-all destroy

build:
	docker compose build worker

run: build
	docker compose --profile worker up

run-kafka:
	docker compose --profile kafka up --force-recreate --renew-anon-volumes

run-monitoring:
	docker compose --profile monitoring up --force-recreate --renew-anon-volumes

run-all:
	docker compose --profile all up --force-recreate --renew-anon-volumes

destroy:
	docker compose down -v
	docker container prune --force