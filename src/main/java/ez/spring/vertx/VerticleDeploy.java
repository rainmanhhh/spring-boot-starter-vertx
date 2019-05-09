package ez.spring.vertx;

import io.vertx.core.Verticle;
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
//
//    public static VerticleDeploy of(String descriptor, String beanQualifier) {
//        VerticleDeploy verticleDeploy = new VerticleDeploy();
//        verticleDeploy.descriptor = descriptor;
//        verticleDeploy.beanQualifier = beanQualifier;
//        return verticleDeploy;
//    }
//
//    public static VerticleDeploy of(String descriptor) {
//        return of(descriptor, null);
//    }
//
//    public static VerticleDeploy of(Class<? extends Verticle> verticleClass, String beanQualifier) {
//        return of(verticleClass.getCanonicalName(), beanQualifier);
//    }
//
//    public static VerticleDeploy of(Class<? extends Verticle> verticleClass) {
//        return of(verticleClass, null);
//    }
//
//    public static VerticleDeploy of() {
//        return of((String) null);
//    }
}
