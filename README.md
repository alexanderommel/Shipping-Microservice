# Shipping-Microservice
 SpringBoot Microservice part of the project Tongue, which is a personal project based on Uber eats system. This microservice is charged of the shipping part of an order fulfillment process, that is: 
 
 1. Receive shipping requests
 2. Find a set of drivers/couriers
 3. Ask anyone to ship the order.
 4. Pick a courier
 5. Lead the courier on his way to the restaurant.
 6. Send an arrival notification message.
 7. Lead the courier on his way to the customer position
 
## Features

- Unit testing, Integration testing, Websockets testing and Security testing.

- Asynchronous Communication between microservices using AMQP

- RabbitMQ Broker

- Publish/Subscribe Communication with customer/driver android apps using STOMP.

- Session Management with Redis 

- JWT Authentication

- Geolocation

- Web Tools for Testing (Test autentication, websockets connection and the main features of the service)

- Delivery Service

- Android app as frontend (Drivers App)

- Logging


## Requirements

1. Redis: This service uses a Redis Server to pull and push data from/to user sessions.
2. RabbitMQ: Tongue uses AMQP for communication between microservices, specifically a RabbitMQ broker.

Optional: Shippin Service Test Client uses an Apache HTTP Server to run web side logic.
