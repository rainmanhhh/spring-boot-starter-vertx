package ez.spring.vertx;

import lombok.Data;

@Data
public class VerticleDeploy extends DeploymentOptionsEx {
    /**
     * verticle descriptor. ex: `groovy:com.xx.yy.ZVerticle`, `service:http://xx.yy/zVerticle.zip`, `someBeanName`
     */
    private String descriptor;
    /**
     * bean qualifier. only valid if {@link #descriptor} is a bean class name
     */
    private String beanQualifier;

    public VerticleDeploy() {
        super();
    }

    public VerticleDeploy(DeploymentOptionsEx other) {
        super(other);
    }
}
