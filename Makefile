.DEFAULT_GOAL := help

# Las recetas corren con las herramientas de Git for Windows (bash, sh, awk, curl...), no con cmd.exe.
# make en Windows ejecuta directo (sin SHELL) los comandos simples, así que además va primero en el PATH.
GIT_DIR ?= C:/Program Files/Git
SHELL := $(GIT_DIR)/bin/bash.exe
.SHELLFLAGS := -ec
export PATH := $(GIT_DIR)/usr/bin;$(PATH)

# JDK para Gradle: si JAVA_HOME no está definido, el que trae Android Studio.
JAVA_HOME ?= $(shell for d in "/c/Program Files/Android/Android Studio"*/jbr; do \
	[ -f "$$d/release" ] && cygpath -m "$$d" && break; done)
export JAVA_HOME

GRADLE := sh ./gradlew
ADB ?= $(shell cygpath -m "$$LOCALAPPDATA")/Android/Sdk/platform-tools/adb.exe
APP_ID := com.valsagnapps.dndapp
DEV_BASE_URL := $(shell sed -n 's/^dndapp.devBaseUrl=//p' local.properties 2>/dev/null | tr -d '\r')

.PHONY: help
help: ## Muestra esta ayuda
	@awk 'BEGIN {FS = ":.*## "} /^##@/ {printf "\n\033[1m%s\033[0m\n", substr($$0, 5)} /^[a-zA-Z_-]+:.*## / {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

##@ App

.PHONY: run
run: install ## Instala la app de debug en el dispositivo conectado y la abre
	"$(ADB)" shell am start -n $(APP_ID)/.MainActivity

.PHONY: install
install: ## Instala la app de debug en el dispositivo conectado
	$(GRADLE) installDebug

.PHONY: apk
apk: ## Compila el APK de debug (app/build/outputs/apk/debug)
	$(GRADLE) assembleDebug

.PHONY: release
release: ## Compila el APK de release (usa dndapp.prodBaseUrl de local.properties)
	$(GRADLE) assembleRelease
	@echo "APK en app/build/outputs/apk/release"

.PHONY: devices
devices: ## Dispositivos conectados por adb
	"$(ADB)" devices -l

.PHONY: logs
logs: ## Sigue los logs de la app (tiene que estar abierta)
	@pid=$$("$(ADB)" shell pidof -s $(APP_ID) | tr -d '\r'); \
	if [ -z "$$pid" ]; then echo "La app no está corriendo (make run)"; exit 1; fi; \
	"$(ADB)" logcat -v color --pid=$$pid

.PHONY: logs-http
logs-http: ## Sigue solo los logs de red (requests, respuestas y errores)
	"$(ADB)" logcat -v color -s DnDHttp DnDRepository

.PHONY: server-check
server-check: ## Verifica que el server de dev responda en dndapp.devBaseUrl (local.properties)
	@if [ -z "$(DEV_BASE_URL)" ]; then echo "Falta dndapp.devBaseUrl en local.properties"; exit 1; fi
	curl -fsS -o /dev/null -w "%{http_code} $(DEV_BASE_URL)api/v1/characters\n" "$(DEV_BASE_URL)api/v1/characters"

##@ Calidad

.PHONY: build
build: ## Build completo: compila debug y release, tests, lint de Android, ktlint y detekt
	$(GRADLE) build

.PHONY: test
test: ## Corre los tests, incluidos los de Compose con Robolectric (T=Patron para filtrar, ej: make test T='*CharacterTest*')
	$(GRADLE) testDebugUnitTest $(if $(T),--tests '$(T)')

.PHONY: lint
lint: ## ktlint + detekt + lint de Android
	$(GRADLE) ktlintCheck detekt lintDebug

.PHONY: format
format: ## Autoformatea con ktlint
	$(GRADLE) ktlintFormat

.PHONY: clean
clean: ## Borra los artefactos de build
	$(GRADLE) clean
