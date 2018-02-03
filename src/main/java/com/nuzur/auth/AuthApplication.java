package com.nuzur.auth;

import com.nuzur.auth.dao.UserDao;
import com.nuzur.auth.service.AuthProvider;
import com.nuzur.common.constant.ErrorNumber;
import com.nuzur.common.domain.User;
import com.nuzur.common.pojo.Result;
import com.nuzur.common.util.PermalinkHandler;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.CorsFilter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * oAuth 2.0 app.
 */
@SpringBootApplication
@RestController
@SessionAttributes("authorizationRequest")
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})

@EnableOAuth2Client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@ComponentScan(basePackages = {"com.nuzur.common.util", "com.nuzur.auth"})
public class AuthApplication extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AuthProvider.class);
    private static final Integer ORDER = -100;

    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PermalinkHandler permalinkHandler;


    @RequestMapping({"/user", "/me"})
    public Principal user(Principal principal) {
        return principal;
    }

    @RequestMapping("/unauthenticated")
    public String unauthenticated() {
        return "redirect:/?error=true";
    }





    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/login**", "/webjars/**", "/user").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringAntMatchers("/logout**")
                .and().addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .formLogin().loginPage("/login").permitAll();
    }

    private OAuth2ClientAuthenticationProcessingFilter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
        filter.setRestTemplate(template);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                client.getResource().getUserInfoUri(), client.getClient().getClientId());

        tokenServices.setPrincipalExtractor(map -> {
            Optional<User> user = Optional.empty();
            String networkId = null;
            if (path.contains("facebook")) {
                networkId = map.get("id").toString();
                user = userDao.getUserByFacebookId(networkId);
            } else if (path.contains("github")) {
                networkId = map.get("id").toString();
                user = userDao.getUserByGithubId(networkId);
            }

            if (user.isPresent()) {
                return user.get();
            } else {
                String email = map.get("email").toString();
                Optional<User> userEmail = userDao.getUserByEmail(email);
                if (userEmail.isPresent()) {
                    if (path.contains("facebook")) {
                        Optional<User> emailUpdated = userDao.updateFacebookId(networkId, userEmail.get().getId());
                        return emailUpdated.get();
                    } else if (path.contains("github")) {
                        Optional<User> emailUpdated = userDao.updateGithubId(networkId, userEmail.get().getId());
                        return emailUpdated.get();
                    }

                } else {
                    User newUser = new User();
                    newUser.setName(map.get("first_name").toString());
                    newUser.setLastName(map.get("last_name").toString());
                    newUser.setEmail(map.get("email").toString());
                    newUser.setType(1);
                    Optional<String> permalink = permalinkHandler.createPermalink(newUser.getName(), User.class, "permalink" , Optional.empty());
                    if (permalink.isPresent()) {
                        newUser.setPermalink(permalink.get());
                    } else {
                        return Result.failedResult(User.class, "Error creating permalink", ErrorNumber.INTERNAL_ERROR);
                    }

                    if (path.contains("facebook")) {
                        newUser.setFacebookId(networkId);
                    } else if (path.contains("github")) {
                        newUser.setGithubId(networkId);
                    }
                    Result<User> newUserResult = userDao.insert(newUser);
                    return newUserResult.getValue().get();
                }
            }

            return map;
        });


        tokenServices.setRestTemplate(template);
        filter.setTokenServices(tokenServices);
        return filter;
    }

    private CompositeFilter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<OAuth2ClientAuthenticationProcessingFilter> filters = new ArrayList<>();
        filters.add(ssoFilter(facebook(), "/login/facebook"));
        filters.add(ssoFilter(github(), "/login/github"));
        filter.setFilters(filters);
        return filter;
    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("github")
    public ClientResources github() {
        return new ClientResources();
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(ORDER);
        return registration;
    }

    @Bean
    public FilterRegistrationBean simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public CacheManager getCacheManager() {
        return CacheManager.getInstance();
    }

    /**
     * Resource Server.
     */
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration
            extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/user").authorizeRequests().anyRequest().authenticated();
        }
    }



}
