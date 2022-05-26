package com.nttdata.bank.account.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nttdata.bank.account.client.CardRest;
import com.nttdata.bank.account.client.CustomerRest;
import com.nttdata.bank.account.client.MovementRest;
import com.nttdata.bank.account.client.ProductRest;
import com.nttdata.bank.account.client.model.Product;
import com.nttdata.bank.account.dto.AccountDTO;
import com.nttdata.bank.account.model.Account;
import com.nttdata.bank.account.repository.AccountRepository;
import com.nttdata.bank.account.service.AccountService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements AccountService{
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private MovementRest movementRest;
	
	@Autowired
	private ProductRest productRest;
	
	@Autowired
	private CustomerRest customerRest;
	
	@Autowired
	private CardRest cardRest;
	
	@Autowired
	private JsonMapper jsonMapper;
	
	
	@Override
	public Flux<AccountDTO> findByCustomerId(String customerId) {
		Flux<AccountDTO> accountDTO = accountRepository.findAccountByCustomerId(customerId).map(a -> convertirAAccountDTO(a));
		return accountDTO.flatMap( account -> 
							Mono.just(account)
							.zipWith(movementRest.findByAccountId(account.get_id())
									.collectList(),
									(a, m) -> {
										a.setMovements(m);
										return a;
									})
							.zipWith(productRest.findById(account.getProductId())
									,(a, p) -> {
										a.setProduct(p);
										return a;
									}));
	}

	@Override
	public Mono<Account> save(Account account) {		
		return customerRest.showCustomerInformationById(account.getCustomerId())
						.flatMap( customer -> {
							Mono<Account> accountMono = Mono.empty();
							if(customer.getType() == 1) {	
								accountMono = accountRepository.findAccountByCustomerId(account.getCustomerId())
									.any(a -> a.getProductId().equals(account.getProductId()))
									.flatMap(value ->
										(value) ? productRest.findById(account.getProductId())
													.filter(product -> product.getType() == 3)
													.switchIfEmpty(Mono.error(new Exception("Ya existe una cuenta con ese producto")))
													.flatMap(product -> accountRepository.save(account))
												: productRest.findById(account.getProductId())
													.flatMap(p -> saveVipOrPyme(p, account, 1))
									);
								
													
							}
							if(customer.getType() == 2) {
								accountMono = productRest.findById(account.getProductId())
												.filter(product -> product.getType() == 2 || product.getType() == 4)
												.switchIfEmpty(Mono.error(new Exception("Un cliente empresarial solo puede tener cuenta corriente o de crédito.")))
												.flatMap(p -> saveVipOrPyme(p, account, 2));
							}
					
					return accountMono;
				}
			 );
	}

	@Override
	public Mono<Account> updateBalance(String id, Double balance, Byte type) {
		return accountRepository.findById(id)
				.flatMap(a -> {
					Mono<Account> accountMono = Mono.empty();
					Double newBalance = 0D;
					if(type == 1) {
						newBalance = a.getBalance() + balance;
						a.setBalance(newBalance);
						accountMono = accountRepository.save(a);
					}
					if(type == 2) {
						if(a.getBalance() >= balance) {
							newBalance = a.getBalance() - balance;
							a.setBalance(newBalance);
							accountMono = accountRepository.save(a);
						}
					}
					return accountMono;
				});
	}

	@Override
	public Mono<Account> findById(String id) {
		return accountRepository.findById(id);
	}
	
	public Mono<Account> saveVipOrPyme(Product product, Account account, int type){
		String tarjetaCredito = "628ec6b1f45016ce6d56424d";
	    Mono<Account> accountMono = Mono.empty();
	    if(type == 1) {
	    	if(product.getCategory() == 3 || product.getCategory() == 2) {
	    		if(product.getCategory() == 2) {
	    			accountMono = cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    				.any(c -> c.getProductId().equals(tarjetaCredito))
		    				.flatMap(value -> 
		    				(value) ? accountRepository.save(account)
		    							.flatMap(ac -> cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    											.filter(ca -> ca.getProductId().equals(tarjetaCredito))
		    											.next()
		    											.flatMap(car -> cardRest.associateAccount(car.get_id(), ac.get_id()))
		    											.map(card -> {
		    												return ac;
		    											}))
	    							: Mono.error(new Exception("Debe tener una tarjeta de credito, para obtener un credito")));
	    		}else {
	    			accountMono = cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    				.any(c -> c.getProductId().equals(tarjetaCredito))
		    				.flatMap(value -> 
	    					(value) ? accountRepository.save(account)
	    							: Mono.error(new Exception("Debe tener una tarjeta de credito, para sacar una cuenta VIP")));
	    		} 		
	    	}else if(product.getCategory() == 4) {
	    		accountMono = Mono.error(new Exception("Un cliente personal no puede tener el producto PYME."));
	    	}else if(product.getCategory() == 1) {
	    		accountMono = accountRepository.save(account);
	    	} else {
	    		accountMono = Mono.error(new Exception("Producto no valido."));
	    	}
	    }
	    if(type == 2) {
	    	if(product.getCategory() == 4 || product.getCategory() == 2) {
	    		/*accountMono = cardRest.showCardInformationByCustomerId(account.getCustomerId())
	    				.any(c -> c.getProductId().equals(tarjetaCredito))
	                    .flatMap(value ->
	                    	(value) ? accountRepository.save(account)
	                    			: Mono.error(new Exception("Debe tener una tarjeta de credito, para obtener este producto"))
	                    );*/
	    		if(product.getCategory() == 2) {
	    			accountMono = cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    				.any(c -> c.getProductId().equals(tarjetaCredito))
		    				.flatMap(value -> 
		    				(value) ? accountRepository.save(account)
		    							.flatMap(ac -> cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    											.filter(ca -> ca.getProductId().equals(tarjetaCredito))
		    											.next()
		    											.flatMap(car -> cardRest.associateAccount(car.get_id(), ac.get_id()))
		    											.map(card -> {
		    												return ac;
		    											}))
	    							: Mono.error(new Exception("Debe tener una tarjeta de credito, para obtener un credito")));
	    		}else {
	    			accountMono = cardRest.showCardInformationByCustomerId(account.getCustomerId())
		    				.any(c -> c.getProductId().equals(tarjetaCredito))
		    				.flatMap(value -> 
	    					(value) ? accountRepository.save(account)
	    							: Mono.error(new Exception("Debe tener una tarjeta de credito, para sacar una cuenta PYME")));
	    		}
	    	}else if(product.getCategory() == 1) {
	    		accountMono = accountRepository.save(account);
	    	}
	    }
	    return accountMono;
	}
	
	/*Casteo de objetos usando JsonMapper*/
	private AccountDTO convertirAAccountDTO(Account account) {
		return jsonMapper.convertValue(account, AccountDTO.class);
	}

}
