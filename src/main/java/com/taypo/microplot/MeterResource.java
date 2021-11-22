package com.taypo.microplot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("${microplot.uri-path:/microplot}")
public class MeterResource {

	private final MeterTracker meterTracker;
	private final MPConfig mpConfig;

	public MeterResource(MeterTracker meterTracker, MPConfig mpConfig) {
		this.meterTracker = meterTracker;
		this.mpConfig = mpConfig;
	}

	@GetMapping("/")
	public ModelAndView home() {
		return new ModelAndView("redirect:"
				+ mpConfig.getUriPath()
				+ "/index.html?basepath="
				+ mpConfig.getUriPath());
	}

	@GetMapping("/config")
	public Map<String, Object> config() {
		return Map.of(
				"includeMetrics", mpConfig.getIncludeMetrics(),
				"typeMap", meterTracker.getTypeMap(),
				"period", mpConfig.getPeriod()
		);
	}

	@GetMapping("/metrics")
	public Map<String, Map<String, Collection<Measurement>>> getMetrics() {
		return meterTracker.getStore();
	}

	@GetMapping("/meter/{name}")
	public Map<String, Collection<Measurement>> getMeterData(@PathVariable String name) {
		return meterTracker.getStore().get(name);
	}

}
