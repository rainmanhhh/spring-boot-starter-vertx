package ez.spring.vertx.deploy;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * deployment options with verticle descriptor and beanQualifier(valid only if descriptor is a bean class name)
 */
public class VerticleDeploy extends DeploymentOptionsEx {
    /**
     * verticle descriptor. ex: `xx.yy.ZVerticle`, `groovy:com.xx.yy.ZVerticle`, `service:http://xx.yy/zVerticle.zip`, `someBeanName`
     */
    private String descriptor;
    /**
     * bean qualifier. valid only if {@link #descriptor} is a bean class name
     */
    @Nullable
    private String beanQualifier;

    public VerticleDeploy() {
        super();
    }

    public VerticleDeploy(DeploymentOptionsEx other) {
        super(other);
        if (other instanceof VerticleDeploy) {
            VerticleDeploy deploy = ((VerticleDeploy) other);
            setDescriptor(deploy.getDescriptor());
            setBeanQualifier(deploy.getBeanQualifier());
        }
    }

    public String getDescriptor() {
        return descriptor;
    }

    public VerticleDeploy setDescriptor(String descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor);
        return this;
    }

    @Nullable
    public String getBeanQualifier() {
        return beanQualifier;
    }

    public VerticleDeploy setBeanQualifier(@Nullable String beanQualifier) {
        this.beanQualifier = beanQualifier;
        return this;
    }
}