package ahc.dms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;

@SpringBootTest
class DmsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private FilterChainProxy filterChainProxy;

	@Test
	public void printFilterOrder() {
		filterChainProxy.getFilterChains().forEach(chain -> {
			System.out.println("\nFilter Order:");
			chain.getFilters().forEach(filter ->
					System.out.println(filter.getClass().getName()));
		});
	}

}
