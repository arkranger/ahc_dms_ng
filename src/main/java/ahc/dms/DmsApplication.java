package ahc.dms;

import ahc.dms.dao.dms.entities.*;
import ahc.dms.dao.dms.repositories.*;
import ahc.dms.payload.dto.UserRoleDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class, // Prevents default single DataSource auto-configuration
        HibernateJpaAutoConfiguration.class, // Stops Hibernate from autoconfiguring based on a single DataSource
        JpaRepositoriesAutoConfiguration.class, // Disables automatic JPA repository scanning
        DataSourceTransactionManagerAutoConfiguration.class // Avoids auto-creation of a default transaction manager
})
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
//public class DmsApplication extends SpringBootServletInitializer implements CommandLineRunner{
public class DmsApplication implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private ObjectMasterRepository omRepository;
    @Autowired
    private ObjectRoleRepository orRepository;

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configure global settings
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD) // Use STRICT for exact matching
                .setAmbiguityIgnored(true) // ignore ambiguous matches
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        // Explicit UserRole to UserRoleDto mapping
        modelMapper.typeMap(UserRole.class, UserRoleDto.class)
                .addMappings(mapper -> {
                    // Explicitly define all mappings
                    //mapper.map(src -> src.getUrId(), UserRoleDto::setUrId);
                    mapper.map(src -> src.getUser().getUserId(), UserRoleDto::setUserId);
                    mapper.map(src -> src.getRole().getRoleId(), UserRoleDto::setRoleId);
                    //mapper.map(src -> src.isStatus(), UserRoleDto::setStatus);
                });

        // Validate the configuration
        modelMapper.validate();
        return modelMapper;
    }

    @Override
    @Transactional(transactionManager = "dmsTransactionManager", isolation = Isolation.SERIALIZABLE)
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

            roleRepository.findByRoleName("ROLE_ECOURT")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName("ROLE_ECOURT");
                        newRole.setStatus(true);
                        return roleRepository.save(newRole);
                    });

            User firstUser = userRepository.findByUsername("11448")
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setName("Vijay Chaurasiya");
                        newUser.setUsername("11448");
                        newUser.setEmail("vijaychaurasiya@gmail.com");
                        newUser.setAbout("admin");
                        newUser.setPhone("9721308763");
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

    @Bean
    public ApplicationListener<ApplicationReadyEvent> filterChainInspector() {
        return event -> {
            FilterChainProxy filterChainProxy = event.getApplicationContext()
                    .getBean(FilterChainProxy.class);

            System.out.println("\n===== SECURITY FILTER CHAIN ORDER =====");
            filterChainProxy.getFilterChains().forEach(chain -> {
                System.out.println("\nDefault filters:");
                chain.getFilters().forEach(filter ->
                        System.out.println("- " + filter.getClass().getSimpleName()));
            });
            System.out.println("===== END FILTER CHAIN =====");
        };
    }

}
