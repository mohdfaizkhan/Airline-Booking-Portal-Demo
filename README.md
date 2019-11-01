

Run the first docker command to pull the Zipkin image from hub.docker.com and 
then the next docker command to start it on port 9411.

$ docker pull openzipkin/zipkin
$ docker run -d -p 9411:9411 openzipkin/zipkin

Validate the setup by accessing the Zipkin web interface on the url: http://localhost:9411/zipkin/. The below screen (Image 1) should open up if there are no issues.

![alt text](https://github.com/mohdfaizkhan/MyRetailStore/blob/master/screenshot/1.png "ZIPKIN-1")

This repository contains Netflix OSS stack, microservices 

- application yml/properties to be loaded from configuration server
- gateway service is entry point (API PROXI)
- load balanced communications between services will happened though gateway
- services use functional approach, spring-cloud-function to expose endpoints
- Flight-booking-site demo use reactive stack + ui in imperative,fully reactive
   

run all OSS services (config-server, gateway-server, service-discovery)

communication will happen as follows <br>
browser -> gateway -> service-discovery -> ui-service -> service-discovery -> gateway -> service-discovery -> microservices  
          
#### Flight booking example 

run all extended services (sale, airport, flight, ui), get access to <br>
http://localhost:8080/
make search from MIA to JAX, play with dates
