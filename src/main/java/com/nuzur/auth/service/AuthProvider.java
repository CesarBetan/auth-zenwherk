package com.nuzur.auth.service;


import com.nuzur.common.domain.User;
import com.nuzur.common.util.PasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * 
 * @author mklfarha
 * AuthProvider for Oauth2
 */

@Service
public class AuthProvider implements AuthenticationProvider {
    
    @Autowired
    @Qualifier("jdbcProvisioning")
    private JdbcTemplate template;

    static final Logger logger = LoggerFactory.getLogger(AuthProvider.class);
    

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String username = (String)authentication.getPrincipal();
        final String password = (String)authentication.getCredentials();

        logger.debug("username" + username);
        List<User> users = template.query("Select * from user where email=? limit 1",
                new Object[]{username}, new BeanPropertyRowMapper<User>(User.class));

        logger.debug("user size" + users.size());
        if (users.size() == 0) {
            throw new BadCredentialsException("user not found with email: " + username);
        }

        User user = users.get(0);
        if (user.getPassword() == null) {
            throw new BadCredentialsException("No password set for user");
        }
        if (!user.getPassword().equals(PasswordHandler.encrypt(password))) {
            throw new BadCredentialsException("Bad Credentials " + username + " " + password);
        }
        user.setPassword(null);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        
    }
    


    @Override
    public boolean supports(Class<?> type) {
        logger.debug("supports class: {}", type);
        if (type.equals(UsernamePasswordAuthenticationToken.class))
            return true;
        return false;
    }

}
