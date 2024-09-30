# Overview

This is an example project to demonstrate how to run simplified shop application in a microservice environment

It contains following components:

## `shop-config-server`

A Spring Cloud Config Server. Requires RabbitMQ to be fully functional (see `docker-compose.yml`)

## `shop-discovery-server`

A Netflix Eureka Discovery Server

## `shop-service`, `greeting-service`

Business microservices. Both are clients of `shop-config-server` and `shop-discovery-server`

Additionally `shop-service` calls `greeting-service` by Feign client (with usage of Eureka server)