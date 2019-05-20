package ez.spring.vertx.deploy;

/**
 * deployment options with verticle descriptor and beanQualifier(valid only if descriptor is a bean class name)
 */
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
        this.descriptor = descriptor;
        return this;
    }

    public String getBeanQualifier() {
        return beanQualifier;
    }

    public VerticleDeploy setBeanQualifier(String beanQualifier) {
        this.beanQualifier = beanQualifier;
        return this;
    }
}
