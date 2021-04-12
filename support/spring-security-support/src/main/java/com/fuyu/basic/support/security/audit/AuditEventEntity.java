package com.fuyu.basic.support.security.audit;

import com.fuyu.basic.commons.StringIdEntity;
import com.fuyu.basic.support.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
@Document(indexName = "audit-event", type = ElasticsearchAuditEventRepository.DEFAULT_ES_TYPE_VALUE)
public class AuditEventEntity extends StringIdEntity {

    private String principal;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String type;

    private Map<String, Object> data;

    public AuditEventEntity() {
    }

    public AuditEventEntity(AuditEvent auditEvent) {
        this.principal = auditEvent.getPrincipal();
        this.type = auditEvent.getType();
        this.data = auditEvent.getData();
        this.setId(UUID.randomUUID().toString());
    }

    public String getPrincipal() {
        return principal;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
