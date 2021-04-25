package com.github.dactiv.basic.captcha.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.RestResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 抽象的验证码服务实现
 *
 * @author maurice
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRedisCaptchaService<E, C extends ExpiredCaptcha> implements CaptchaService {

    /**
     * 默认提交验证码的参数名称
     */
    private static final String DEFAULT_CAPTCHA_PARAM_NAME = "captchaParamName";

    /**
     * 默认绑定 token 的过期时间
     */
    private static final Duration DEFAULT_BUILD_TOKEN_EXPIRE_TIME = Duration.ofSeconds(600);

    /**
     * 默认验证码重试时间
     */
    private static final Duration DEFAULT_CAPTCHA_RETRY_TIME = Duration.ofSeconds(60);

    /**
     * 泛型实体class
     */
    private final Class<E> entityClass;

    /**
     * 验证码类型
     */
    private final Class<C> captchaClass;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储在 redis 的绑定 token key 名称
     */
    @Value("${spring.application.captcha.token.build.key-prefix:captcha:build:token:}")
    private String buildTokenKeyPrefix;

    /**
     * 验证绑定 token 的参数后缀名
     */
    @Value("${spring.application.captcha.token.build.param-name-suffix:captchaToken}")
    private String tokenParamNameSuffix;

    @Qualifier("mvcValidator")
    @Autowired(required = false)
    private Validator validator;

    /**
     * 抽象的验证码服务实现
     */
    public AbstractRedisCaptchaService() {

        Type type = this.getClass().getGenericSuperclass();

        this.entityClass = (Class<E>) ((ParameterizedType) type).getActualTypeArguments()[0];

        this.captchaClass = (Class<C>) ((ParameterizedType) type).getActualTypeArguments()[1];
    }

    @Override
    public BuildToken generateToken(String deviceIdentified) {

        Map<String, Object> args = getCreateArgs();

        String value = DigestUtils.md5Hex(deviceIdentified);

        SimpleBuildToken token = new SimpleBuildToken(deviceIdentified, value, getType(), getTokenParamName(), args);

        saveBuildToken(token);

        return token;
    }

    @Override
    public void saveBuildToken(BuildToken token) {
        String key = getBuildTokenKey(token.getToken());
        token.setExpireTime(getBuildTokenExpireTime());
        redisTemplate.opsForValue().set(key, token, token.getExpireTime());
    }

    /**
     * 获取绑定 token 过期时间
     *
     * @return 过期时间
     */
    private Duration getBuildTokenExpireTime() {
        return DEFAULT_BUILD_TOKEN_EXPIRE_TIME;
    }

    /**
     * 获取验证码过期时间
     *
     * @return 过期时间（单位:秒）
     */
    protected abstract Duration getCaptchaExpireTime();

    @Override
    public String getTokenParamName() {
        return "_" + getType() + StringUtils.capitalize(tokenParamNameSuffix);
    }

    public RestResult<Map<String, Object>> verify(HttpServletRequest request) {

        String token = request.getParameter(getTokenParamName());

        // 获取绑定 token 值
        BuildToken buildToken = getBuildToken(token);

        if (buildToken == null) {
            return new RestResult<>(
                    "验证码 token 已过期",
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    RestResult.ERROR_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );
        }

        // 获取存储在 redis 的绑定 token
        C exist = getCaptcha(buildToken);

        if (StringUtils.isNotEmpty(getUsernameParamName())) {

            String username = exist.getUsername();
            String requestUsername = request.getParameter(getUsernameParamName());

            if (!StringUtils.equals(requestUsername, username)) {
                return new RestResult<>(
                        "验证码不正确",
                        HttpStatus.BAD_REQUEST.value(),
                        RestResult.ERROR_EXECUTE_CODE,
                        new LinkedHashMap<>()
                );
            }
        }

        String requestCaptcha = request.getParameter(getCaptchaParamName());

        return verify(buildToken, requestCaptcha);
    }

    @Override
    public RestResult<Map<String, Object>> verify(BuildToken buildToken, String requestCaptcha) {

        // 获取存储在 redis 的绑定 token
        C exist = getCaptcha(buildToken);

        try {

            // 如果没有，表示超时，需要客户端重新生成一个
            if (exist == null || exist.isExpired()) {
                return new RestResult<>(
                        "验证码已过期",
                        HttpStatus.REQUEST_TIMEOUT.value(),
                        RestResult.ERROR_EXECUTE_CODE,
                        new LinkedHashMap<>()
                );
            }

            // 匹配验证码是否通过
            if (matchesCaptcha(requestCaptcha, exist)) {
                // 成功后删除 绑定 token
                deleteBuildToken(buildToken);

                if (!isMatchesFailureDeleteCaptcha()) {
                    deleteCaptcha(buildToken);
                }

                return new RestResult<>(
                        "验证通过",
                        HttpStatus.OK.value(),
                        RestResult.SUCCESS_EXECUTE_CODE,
                        new LinkedHashMap<>()
                );
            }

            return new RestResult<>(
                    "验证码不正确",
                    HttpStatus.BAD_REQUEST.value(),
                    RestResult.ERROR_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );

        } catch (Exception e) {
            return new RestResult<>(
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    RestResult.ERROR_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );
        } finally {
            if (isMatchesFailureDeleteCaptcha()) {
                deleteCaptcha(buildToken);
            }
        }
    }

    /**
     * 是否校验验证码失败直接删除当前验证码信息
     *
     * @return true 是，否则 false
     */
    protected boolean isMatchesFailureDeleteCaptcha() {
        return true;
    }

    /**
     * 删除绑定 token
     *
     * @param buildToken 绑定 token
     */
    protected void deleteBuildToken(BuildToken buildToken) {
        String key = getBuildTokenKey(buildToken.getToken());
        redisTemplate.delete(key);
    }

    @Override
    public Object generateCaptcha(HttpServletRequest request) throws Exception {

        BuildToken buildToken = getBuildToken(request);

        if (buildToken == null) {
            return new RestResult<>(
                    "验证码 token 已过期",
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    RestResult.ERROR_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );
        }

        C exist = getCaptcha(buildToken);

        // 检查一下 redis 里是否已经生成过验证码，如果生成过，判断是已经满足了重试时间，如果不是，返回错误信息
        if (exist != null && Reusable.class.isAssignableFrom(exist.getClass())) {

            Reusable reusable = (Reusable) exist;

            if (!reusable.isRetry()) {

                return new RestResult<>(
                        "验证码未到可生成时间",
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        RestResult.ERROR_EXECUTE_CODE,
                        new LinkedHashMap<>()
                );
            }

        }

        saveBuildToken(buildToken);

        String key = getBuildTokenKey(buildToken.getToken());
        redisTemplate.opsForValue().set(key, buildToken, getBuildTokenExpireTime());

        E entity = entityClass.newInstance();

        WebDataBinder binder = new WebDataBinder(entity, entityClass.getSimpleName());

        MutablePropertyValues mutablePropertyValues = new MutablePropertyValues(request.getParameterMap());
        // 根据实体 class 绑定 request 的参数到实体中
        binder.bind(mutablePropertyValues);
        // 验证参数是否正确
        if (validator != null) {
            binder.setValidator(validator);
            binder.validate();
            if (binder.getBindingResult().hasErrors()) {
                throw new BindException(binder.getBindingResult());
            }
        }

        // 生成验证码
        GenerateCaptchaResult result = generateCaptcha(buildToken, entity);

        C captcha = ClassUtils.newInstance(captchaClass);

        captcha.setExpireTime(getCaptchaExpireTime());
        captcha.setCaptcha(result.getCaptchaValue());

        if (StringUtils.isNotEmpty(getUsernameParamName())) {

            String username = request.getParameter(getUsernameParamName());

            if (StringUtils.isEmpty(username)) {
                return new RestResult<>(
                        getUsernameParamName() + "参数不能为空",
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        RestResult.ERROR_EXECUTE_CODE,
                        new LinkedHashMap<>()
                );
            }

            captcha.setUsername(username);
        }

        if (ReusableCaptcha.class.isAssignableFrom(captcha.getClass())) {
            ReusableCaptcha reusableCaptcha = Casts.cast(captcha);
            reusableCaptcha.setRetryTime(getRetryTime());
        }

        String captchaKey = getCaptchaKey(buildToken);

        redisTemplate.opsForValue().set(captchaKey, captcha, captcha.getExpireTime());

        return result.getResult();
    }

    @Override
    public boolean isSupport(HttpServletRequest request) {

        try {
            BuildToken buildToken = getBuildToken(request);
            return buildToken != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取可重试时间
     *
     * @return 重试时间（单位：秒）
     */
    private Duration getRetryTime() {
        return DEFAULT_CAPTCHA_RETRY_TIME;
    }

    /**
     * 获取存储在 redis 的验证码实体 key 名称
     *
     * @param buildToken 绑定 token
     * @return key 名称
     */
    private String getCaptchaKey(BuildToken buildToken) {
        return buildTokenKeyPrefix + getType() + ":captcha:" + buildToken.getToken();
    }

    /**
     * 获取验证码实体
     *
     * @param buildToken 绑定 token
     * @return 验证码实体
     */
    protected C getCaptcha(BuildToken buildToken) {
        String key = getCaptchaKey(buildToken);
        return Casts.cast(redisTemplate.opsForValue().get(key));
    }

    /**
     * 删除验证码
     *
     * @param buildToken 绑定 token
     */
    protected void deleteCaptcha(BuildToken buildToken) {
        String key = getCaptchaKey(buildToken);
        redisTemplate.delete(key);
    }

    /**
     * 生成验证码
     *
     * @param buildToken 绑定 token
     * @param entity     泛型实体
     * @return 生成验证码结果集
     * @throws Exception 生成错误时抛出
     */
    protected abstract GenerateCaptchaResult generateCaptcha(BuildToken buildToken, E entity) throws Exception;

    /**
     * 匹配验证码是否正确
     *
     * @param requestCaptcha 提交过来的验证码
     * @param captcha        当前验证码
     * @return true 是，否则 false
     */
    protected boolean matchesCaptcha(String requestCaptcha, C captcha) {
        // 匹配验证码
        return StringUtils.equalsIgnoreCase(captcha.getCaptcha(), requestCaptcha);

    }

    /**
     * 获取存储在 redis 的生成验证码的 token key 名称
     *
     * @param token token 值
     * @return key 名称
     */
    public String getBuildTokenKey(String token) {
        return buildTokenKeyPrefix + getType() + ":" + token;
    }

    @Override
    public BuildToken getBuildToken(String token) {
        String key = getBuildTokenKey(token);

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            throw new ServiceException("找不到 token 为[" + token + "]的记录");
        }

        return Casts.cast(value);
    }

    /**
     * 获取绑定 token
     *
     * @param request http servlet request
     * @return 绑定 token
     */
    public BuildToken getBuildToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());

        BuildToken buildToken = getBuildToken(token);

        if (!buildToken.getType().equals(getType())) {
            return null;
        }

        return buildToken;
    }

    @Override
    public Map<String, Object> getCreateArgs() {

        Map<String, Object> args = new LinkedHashMap<>();

        Map<String, Object> post = createPostArgs();

        if (MapUtils.isNotEmpty(post)) {
            args.put("post", post);
        }

        Map<String, Object> generate = createGenerateArgs();

        if (MapUtils.isNotEmpty(generate)) {
            args.put("generate", generate);
        }

        return args;
    }

    /**
     * 构造 post 参数信息
     *
     * @return 构造参数 map
     */
    protected Map<String, Object> createPostArgs() {
        Map<String, Object> post = new LinkedHashMap<>();

        Object value = getCaptchaParamName();

        if (Objects.nonNull(value)) {
            post.put(DEFAULT_CAPTCHA_PARAM_NAME, value);
        }

        return post;
    }

    /**
     * 获取生成验证码时需要的构造参数
     *
     * @return 构造参数 map
     */
    protected Map<String, Object> createGenerateArgs() {
        return new LinkedHashMap<>();
    }

}
