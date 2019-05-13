package ez.spring.vertx.deploy;

import lombok.Data;

/**
 * deployment options with verticle descriptor and beanQualifier(valid only if descriptor is a bean class name)
 */
@Data
public class VerticleDeploy extends DeploymentOptionsEx {
    /**
     * verticle descriptor. ex: `groovy:com.xx.yy.ZVerticle`, `service:http://xx.yy/zVerticle.zip`, `someBeanName`
     */
    private String descriptor;
    /**
     * bean qualifier. valid only if {@link #descriptor} is a bean class name
     */
    private String beanQualifier;

    public VerticleDeploy() {
        super();
    }

    public VerticleDeploy(DeploymentOptionsEx other) {
        super(other);
    }
}
