package com.taypo.microplot;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("microplot")
public class MPConfig {
	private String[] includeMetrics;
	private int keepRecordsMax = 200;
	private int period = 5000;

	public String[] getIncludeMetrics() {
		return includeMetrics;
	}

	public void setIncludeMetrics(String[] includeMetrics) {
		this.includeMetrics = includeMetrics;
	}

	@Bean
	public MeterTracker meterTracker(MeterRegistry meterRegistry) {
		return new MeterTracker(meterRegistry, this);
	}

	@Bean
	public MeterResource meterResource(MeterTracker meterTracker) {
		return new MeterResource(meterTracker);
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
