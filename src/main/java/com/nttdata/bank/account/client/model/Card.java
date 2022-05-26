package com.nttdata.bank.account.client.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
	
	private String _id;
	
	private String customerId;
	
	private String productId;
	
	private String mainAccount;
	
	private List<String> associatedAccounts = new ArrayList<String>();
	
	/*@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate createAt = LocalDate.now();*/
	private String createAt;
	
	/*Tarjeta credito*/
	private Double amount;
	
	private Double amountAvailable;

}
