package com.nuzur.auth.dao;

import com.nuzur.common.dao.AbstractDao;
import com.nuzur.common.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * User DAO.
 */
@Component
public class UserDao extends AbstractDao<User> {

    @Autowired
    @Qualifier("jdbcProvisioning")
    private JdbcTemplate template;

    public List<User> getUsersByEmail(String email) {
        return template.query("Select * from user where email=? limit 1",
                new Object[]{email}, new BeanPropertyRowMapper<>(User.class));
    }


    public Optional<User> getUserByEmail(String email) {
        List<User> users = template.query("Select * from user where email=? limit 1",
                new Object[]{email}, new BeanPropertyRowMapper<>(User.class));
        if (users == null || users.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    public Optional<User> getUserByFacebookId(String facebookId) {
        List<User> users = template.query("Select * from user where facebook_id=? limit 1",
                new Object[]{facebookId}, new BeanPropertyRowMapper<>(User.class));
        if (users == null || users.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    public Optional<User> getUserByGithubId(String githubId) {
        List<User> users = template.query("Select * from user where github_id=? limit 1",
                new Object[]{githubId}, new BeanPropertyRowMapper<>(User.class));
        if (users == null || users.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    public Optional<User> updateFacebookId(String facebookId, Long id) {
        template.update("UPDATE user SET facebook_id=?  WHERE id = ? ", facebookId, id);
        return getById(id).getValue();
    }

    public Optional<User> updateGithubId(String githubId, Long id) {
        template.update("UPDATE user SET github_id=?  WHERE id = ? ", githubId, id);
        return getById(id).getValue();
    }

    @Override
    protected JdbcTemplate getJdbcTemplate() {
        return template;
    }

    @Override
    public String getRestName() {
        return "user";
    }

    @Override
    public Class<User> getType() {
        return User.class;
    }

    @Override
    public String getTableName() {
        return "user";
    }
}
