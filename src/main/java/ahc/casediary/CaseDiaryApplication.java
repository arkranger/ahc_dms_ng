package ahc.casediary;

import ahc.casediary.dao.entities.Role;
import ahc.casediary.dao.entities.User;
import ahc.casediary.dao.entities.UserRole;
import ahc.casediary.dao.repositories.RoleRepository;
import ahc.casediary.dao.repositories.UserRepository;
import ahc.casediary.dao.repositories.UserRoleRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class CaseDiaryApplication implements CommandLineRunner {


	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserRoleRepository userRoleRepository;

	public static void main(String[] args) {
		SpringApplication.run(CaseDiaryApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		// Configure global settings
		modelMapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STANDARD) // Use STRICT for exact matching
				.setAmbiguityIgnored(true) // ignore ambiguous matches
				.setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
		// Validate the configuration
		modelMapper.validate();
		return modelMapper;
	}


	@Override
	@Transactional
	public void run(String... args) {

		try {
			// Create and save roles once
			Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
					.orElseGet(() -> {
						Role newRole = new Role();
						newRole.setRoleName("ROLE_ADMIN");
						newRole.setStatus(true);
						return roleRepository.save(newRole);
					});

			roleRepository.findByRoleName("ROLE_USER")
					.orElseGet(() -> {
						Role newRole = new Role();
						newRole.setRoleName("ROLE_USER");
						newRole.setStatus(true);
						return roleRepository.save(newRole);
					});

			User firstUser = userRepository.findByUsername("11448")
					.orElseGet(() -> {
						User newUser = new User();
						newUser.setName("Amit Verma");
						newUser.setUsername("11448");
						newUser.setEmail("amitvarmaone@gmail.com");
						newUser.setAbout("admin");
						newUser.setPhone("8601837554");
						newUser.setPassword(passwordEncoder.encode("1234"));
						return userRepository.saveAndFlush(newUser);
					});

			// For each role, check if it exists first
			if (!userRoleRepository.existsByUserAndRole(firstUser, adminRole)) {
				userRoleRepository.save(new UserRole(firstUser, adminRole, true));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
