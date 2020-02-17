package ez.spring.vertx.bean;

public class BeanNotFoundException extends RuntimeException {
  public BeanNotFoundException(BeanGetterFinalStep<?> beans) {
    super(
      "descriptor: " + beans.getDescriptor() +
        ", beanType: " + beans.getBeanType() +
        ", qualifierClass: " + beans.getQualifierClass() +
        ", qualifierValue: " + beans.getQualifierValue()
    );
  }
}
