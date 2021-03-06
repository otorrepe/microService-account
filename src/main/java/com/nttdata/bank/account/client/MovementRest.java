package com.nttdata.bank.account.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nttdata.bank.account.client.model.Movement;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;

@ReactiveFeignClient(name = "service-movement", url = "localhost:9960")
public interface MovementRest {
	
	@GetMapping
	public Flux<Movement> findByAccountId(@RequestParam String accountId);

}
