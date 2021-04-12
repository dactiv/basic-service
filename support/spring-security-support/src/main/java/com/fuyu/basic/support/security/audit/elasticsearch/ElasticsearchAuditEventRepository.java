package com.fuyu.basic.support.security.audit.elasticsearch;

import com.fuyu.basic.support.security.audit.AuditEventEntity;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * es 审计时间仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository implements AuditEventRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    public final static String DEFAULT_ES_INDEX = "audit-event";

    public final static String DEFAULT_ES_TYPE_VALUE = "_doc";

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    private final SecurityProperties securityProperties;

    public ElasticsearchAuditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                             SecurityProperties securityProperties) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public void add(AuditEvent event) {

        String index = getIndexString(event).toLowerCase();

        try {

            AuditEventEntity auditEventEntity = new AuditEventEntity(event);

            if (!auditEventEntity.getPrincipal().equals(securityProperties.getUser().getName())) {

                IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder()
                        .withId(auditEventEntity.getId())
                        .withObject(auditEventEntity);

                elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of(index));
            }

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    public String getIndexString(AuditEvent event) {

        LocalDateTime time = LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault());

        String principal = event.getPrincipal();

        if (StringUtils.contains(principal, ":")) {
            principal = StringUtils.substringAfter(event.getPrincipal(), ":");
        }

        return DEFAULT_ES_INDEX + "-" + principal + "-" + time.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        String index = DEFAULT_ES_INDEX + "-*";

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (StringUtils.isNotEmpty(principal)) {
            boolQuery.must(QueryBuilders.termQuery("principal", principal));
            index = DEFAULT_ES_INDEX + "-" + principal + "-*";
        }

        if (StringUtils.isNotEmpty(type)) {
            boolQuery.must(QueryBuilders.termQuery("type", type));
        }

        if (after != null) {
            boolQuery.must(QueryBuilders.rangeQuery("timestamp").gte(after));
        }

        builder.withQuery(boolQuery);

        List<AuditEventEntity> auditEventEntities = elasticsearchRestTemplate.queryForList(
                builder.build(),
                AuditEventEntity.class,
                IndexCoordinates.of(index)
        );

        return auditEventEntities
                .stream()
                .map(a -> new AuditEvent(a.getCreationTime().toInstant(), a.getPrincipal(), a.getType(), a.getData()))
                .collect(Collectors.toList());
    }

}
