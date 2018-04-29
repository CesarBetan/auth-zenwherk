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
    @Value("${spring.datasource.provisioning.username}")
    private String userProv;

    @Value("${spring.datasource.provisioning.password}")
    private String passwordProv;

    @Value("${spring.datasource.provisioning.url}")
    private String dataSourceUrlProv;

    @Value("${spring.datasource.provisioning.driverClassName}")
    private String driverClassNameProv;

    @Value("${spring.datasource.provisioning.connectionTestQuery}")
    private String connectionTestQueryProv;

    @Value("${spring.datasource.provisioning.maximumPoolSize}")
    private String maximumPoolSizeProv;



    //auth
    @Value("${spring.datasource.auth.username}")
    private String userAuth;

    @Value("${spring.datasource.auth.password}")
    private String passwordAuth;

    @Value("${spring.datasource.auth.url}")
    private String dataSourceUrlAuth;

    @Value("${spring.datasource.auth.driverClassName}")
    private String driverClassNameAuth;

    @Value("${spring.datasource.auth.connectionTestQuery}")
    private String connectionTestQueryAuth;

    @Value("${spring.datasource.auth.maximumPoolSize}")
    private String maximumPoolSizeAuth;




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
        //configProps.setProperty("maximumPoolSize", maximumPoolSizeAuth);

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
        //configProps.setProperty("maximumPoolSize", maximumPoolSizeProv);

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
