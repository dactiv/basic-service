package com.fuyu.basic.commons;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 模拟数据测试类
 *
 * @author maurice
 */
public class SimulationDataTest {

    private DataSource dataSource;

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected void executeScript(Connection connection, String... sqlResourcePaths) throws SQLException {

        for (String sqlResourcePath : sqlResourcePaths) {
            Resource resource = resourceLoader.getResource(sqlResourcePath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
        connection.close();
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
