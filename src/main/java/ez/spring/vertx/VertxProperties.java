package ez.spring.vertx;

import ez.spring.util.Provider;
import ez.spring.util.Switch;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.metrics.MetricsOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("vertx")
public class VertxProperties extends VertxOptions {
    private String clusterManagerClass = null;

    private MetricsProperties metrics = new MetricsProperties();

    @NestedConfigurationProperty
    private EventBusOptions eventBus = getEventBusOptions();

    @NestedConfigurationProperty
    private AddressResolverOptions addressResolver = getAddressResolverOptions();

    public String getClusterManagerClass() {
        return clusterManagerClass;
    }

    public void setClusterManagerClass(String clusterManagerClass) throws Exception {
        this.clusterManagerClass = clusterManagerClass;
        setClusterManager(Provider.createInstance(clusterManagerClass));
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsProperties metrics) throws Exception {
        this.metrics = metrics;
        super.setMetricsOptions(metrics.toOptions());
    }

    public static class MetricsProperties extends Switch {
        private String factoryClass = null;

        public String getFactoryClass() {
            return factoryClass;
        }

        public void setFactoryClass(String factoryClass) {
            this.factoryClass = factoryClass;
        }

        MetricsOptions toOptions() throws Exception {
            return new MetricsOptions()
                    .setEnabled(isEnabled())
                    .setFactory(Provider.createInstance(factoryClass));
        }
    }

    public EventBusOptions getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBusOptions eventBus) {
        setEventBusOptions(eventBus);
    }

    public AddressResolverOptions getAddressResolver() {
        return addressResolver;
    }

    public void setAddressResolver(AddressResolverOptions addressResolver) {
        setAddressResolverOptions(addressResolver);
    }
}
