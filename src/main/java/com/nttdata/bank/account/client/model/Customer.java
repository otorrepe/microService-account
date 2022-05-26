package com.nttdata.bank.account.client.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
	
	@Id
	private String _id;
	
	private Byte type;
	
	private String description;
	
	private String name;

	private String document;
	
	private String createAt;

}
