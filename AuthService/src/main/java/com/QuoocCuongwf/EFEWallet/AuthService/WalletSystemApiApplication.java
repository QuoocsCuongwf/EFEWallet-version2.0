package com.QuoocCuongwf.EFEWallet.AuthService;

import com.QuoocCuongwf.EFEWallet.AuthService.service.MessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WalletSystemApiApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(WalletSystemApiApplication.class, args);
		MessageService messageService = context.getBean(MessageService.class);
		messageService.sendMessage("Test","Hello");
	}

}
