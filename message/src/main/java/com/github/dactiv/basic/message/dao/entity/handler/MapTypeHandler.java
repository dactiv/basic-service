package com.github.dactiv.basic.message.dao.entity.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.exception.ServiceException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MapTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String value = objectMapper.writeValueAsString(parameter);
            ps.setString(i, value);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {

        String result = rs.getString(columnName);

        try {
            return objectMapper.readValue(result, Map.class);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {

        String result = rs.getString(columnIndex);

        try {
            return objectMapper.readValue(result, Map.class);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {

        String result = cs.getString(columnIndex);

        try {
            return objectMapper.readValue(result, Map.class);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e);
        }
    }

}
