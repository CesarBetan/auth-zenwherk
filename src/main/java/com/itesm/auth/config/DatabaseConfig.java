package com.itesm.auth.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 
 * @author mklfarha
 * Configures the db connection.
 * 
 */
@Configuration
public class DatabaseConfig {

    //prov
    private String userProv = "zenwherk";

    private String passwordProv = "zenwherk-password";

    private String dataSourceUrlProv = "jdbc:mysql://zenwherk.cbt3wxqgkpus.us-east-1.rds.amazonaws.com:3306/zenwherk_api?useSSL=false";

    private String driverClassNameProv = "com.mysql.jdbc.Driver";

    private String connectionTestQueryProv = "SELECT 1";

    private String maximumPoolSizeProv = "5";



    //auth
    private String userAuth = "zenwherk";

    private String passwordAuth = "zenwherk-password";

    private String dataSourceUrlAuth = "jdbc:mysql://zenwherk-auth.cbt3wxqgkpus.us-east-1.rds.amazonaws.com:3306/zenwherk_auth?useSSL=false";

    private String driverClassNameAuth = "com.mysql.jdbc.Driver";

    private String connectionTestQueryAuth = "SELECT 1";

    private String maximumPoolSizeAuth = "5";




    @Bean(name = "dsAuth")
    @Primary
    public DataSource authDataSource() {
        Properties dsProps = new Properties();
        dsProps.setProperty("url", dataSourceUrlAuth);
        dsProps.setProperty("user", userAuth);
        dsProps.setProperty("password", passwordAuth);

        Properties configProps = new Properties();
        configProps.setProperty("connectionTestQuery", connectionTestQueryAuth);
        configProps.setProperty("driverClassName", driverClassNameAuth);
        configProps.setProperty("jdbcUrl", dataSourceUrlAuth);
        configProps.setProperty("maximumPoolSize", maximumPoolSizeAuth);

        HikariConfig hc = new HikariConfig(configProps);
        hc.setDataSourceProperties(dsProps);
        return new HikariDataSource(hc);
    }

    @Bean(name = "jdbcAuth")
    @Autowired
    public JdbcTemplate authJdbcTemplate(@Qualifier("dsAuth") DataSource dsProvisioning) {
        return new JdbcTemplate(dsProvisioning);
    }


    @Bean(name = "dsProvisioning")
    public DataSource primaryDataSource() {
        Properties dsProps = new Properties();
        dsProps.setProperty("url", dataSourceUrlProv);
        dsProps.setProperty("user", userProv);
        dsProps.setProperty("password", passwordProv);

        Properties configProps = new Properties();
        configProps.setProperty("connectionTestQuery", connectionTestQueryProv);
        configProps.setProperty("driverClassName", driverClassNameProv);
        configProps.setProperty("jdbcUrl", dataSourceUrlProv);
        configProps.setProperty("maximumPoolSize", maximumPoolSizeProv);

        HikariConfig hc = new HikariConfig(configProps);
        hc.setDataSourceProperties(dsProps);
        return new HikariDataSource(hc);
    }

    @Bean(name = "jdbcProvisioning")
    @Autowired
    public JdbcTemplate provJdbcTemplate(@Qualifier("dsProvisioning") DataSource dsProvisioning) {
        return new JdbcTemplate(dsProvisioning);
    }



}
