This repository contains Netflix OSS stack, microservices 

- application yml/properties to be loaded from configuration server
- gateway service is entry point (API PROXI)
- load balanced communications between services will happened though gateway
- services use functional approach, spring-cloud-function to expose endpoints
- Flight-booking-site demo use reactive stack + ui in imperative,fully reactive
   

run all OSS services (config-server, gateway-server, service-discovery)

communication will happen as follows <br>
browser -> gateway -> service-discovery -> ui-service -> service-discovery -> gateway -> service-discovery -> microservices  
          
#### extended example 

run all extended services (sale, airport, flight, ui), get access to <br>
http://localhost:8080/
make search from MIA to JAX, play with dates