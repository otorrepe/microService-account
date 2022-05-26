package com.nttdata.bank.account.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nttdata.bank.account.client.model.Card;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "service-card", url = "localhost:9935")
public interface CardRest {
	
	@GetMapping("/card")
	public Flux<Card> showCardInformationByCustomerId(@RequestParam String customerId);
	
	@PutMapping("/card")
	public Mono<Card> associateAccount(@RequestParam String id, 
										@RequestParam String accountId);

}
