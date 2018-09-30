package cn.ez.vertx;

abstract public class Factory<T> {
    private String className = null;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @SuppressWarnings("unchecked")
    public T createInstance() throws Exception {
        return className == null ? null : (T) Class.forName(className).getConstructor().newInstance();
    }
}
