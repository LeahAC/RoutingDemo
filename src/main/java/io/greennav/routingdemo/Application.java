package io.greennav.routingdemo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.greennav.routingdemo.service.Network;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public InitializingBean init(Network network) {
		// Starts the initialization of the routing network
		// Please provide file 'test.osm.pbf' on the working directory
		return network::initialize;
	}

}
