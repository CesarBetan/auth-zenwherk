package com.itesm.auth.dao;


import com.itesm.auth.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * User DAO las queries que necesito para autenticar al usuario
 */
@Component
public class UserDao {

    @Autowired
    @Qualifier("jdbcProvisioning")
    private JdbcTemplate template;

    public List<User> getUsersByEmail(String email) {
        return template.query("Select * from user where email=? limit 1",
                new Object[]{email}, new BeanPropertyRowMapper<>(User.class));
    }


}
