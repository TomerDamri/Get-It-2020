package GetIt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages= {"GetIt.controller"})
public class GetItMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(GetItMainApplication.class, args);
	}

}
