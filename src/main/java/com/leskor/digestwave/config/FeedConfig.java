package com.leskor.digestwave.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class FeedConfig {
    @Bean
    public FeedProperties feedProperties() {
        try {
            Resource resource = new ClassPathResource("feeds.yml");
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            PropertySource<?> yamlTestProperties = loader.load("feeds", resource).getFirst();

            MutablePropertySources propertySources = new MutablePropertySources();
            propertySources.addFirst(yamlTestProperties);

            ConfigurableEnvironment environment = new StandardEnvironment();
            for (PropertySource<?> ps : propertySources) {
                environment.getPropertySources().addFirst(ps);
            }

            return Binder.get(environment)
                    .bind("feeds", Bindable.of(FeedProperties.class))
                    .orElseThrow(IllegalStateException::new);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load feed feeds.yml", e);
        }
    }
}
