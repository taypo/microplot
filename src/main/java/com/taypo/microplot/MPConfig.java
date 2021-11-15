package com.taypo.microplot;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties("microplot")
public class MPConfig implements WebMvcConfigurer {
	private String uriPath = "/micrometer";
	private String[] includeMetrics;
	private int keepRecordsMax = 200;
	private int period = 5000;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
				.addResourceHandler(uriPath + "/**")
				.addResourceLocations("classpath:/web/");
	}

	@Bean
	public MeterTracker meterTracker(MeterRegistry meterRegistry) {
		return new MeterTracker(meterRegistry, this);
	}

	@Bean
	public MeterResource meterResource(MeterTracker meterTracker) {
		return new MeterResource(meterTracker, this);
	}

	public String getUriPath() {
		return uriPath;
	}

	public void setUriPath(String uriPath) {
		this.uriPath = uriPath;
	}

	public String[] getIncludeMetrics() {
		return includeMetrics;
	}

	public void setIncludeMetrics(String[] includeMetrics) {
		this.includeMetrics = includeMetrics;
	}

	public int getKeepRecordsMax() {
		return keepRecordsMax;
	}

	public void setKeepRecordsMax(int keepRecordsMax) {
		this.keepRecordsMax = keepRecordsMax;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
}
