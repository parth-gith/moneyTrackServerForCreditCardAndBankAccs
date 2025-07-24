package com.parth.money.moneyServer;
import com.parth.money.moneyServer.Utils.PreloaderRedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableCaching
@EnableAsync
public class MoneyServerApplication {

	@Autowired
	PreloaderRedisCache preloaderRedisCache;

	public static void main(String[] args) {
		SpringApplication.run(MoneyServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initiateRedisCachePreload() {
		preloaderRedisCache.preloadRedisCache();
	}


}
