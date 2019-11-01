# This repository contains Netflix OSS stack, microservices 

# Install and run Zipkin
Run the first docker command to pull the Zipkin image from hub.docker.com and 
then the next docker command to start it on port 9411.

$ docker pull openzipkin/zipkin

$ docker run -d -p 9411:9411 openzipkin/zipkin

Validate the setup by accessing the Zipkin web interface on the url: http://localhost:9411/zipkin/. The below screen (Image 1) should open up if there are no issues.

![alt text](https://github.com/mohdfaizkhan/Airline-Booking-Portal-Demo/blob/master/Images/Zipkin.PNG "ZIPKIN-1")

# Installing ELK Stack
This install will be using the image `sebp/elk`, on this image we will be making changes to disable SSL and setup indexes for Elastic search on the Log-stash configuration files.
Go to the Docker file floder and Execute the below docker commands to build the image with tag `local-elk` and start all three components.

docker build . --tag local-elk

docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk local-elk

The "docker run", command starts up Kibana on port 5601, ElasticSearch on port 9200 and LogStash on port 5044.
Validate the kibana setup by accessing the web console on url ‘http://localhost:5601’. 

![alt text](https://github.com/mohdfaizkhan/Airline-Booking-Portal-Demo/blob/master/Images/Kibana-Dashbord.PNG "Kibana Dashboard")

Validate Elasticsearch with the below curl command

curl http://localhost:9200/_cat/indices

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
