package com.taypo.microplot;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public Map<String, Serializable> config() {
		return Map.of(
				"includeMetrics", mpConfig.getIncludeMetrics(),
				"period", mpConfig.getPeriod()
		);
	}

	@GetMapping("/meter/{name}")
	public Collection getMeterData(@PathVariable String name) {
		return meterTracker.getStore().get(name);
	}

	@GetMapping("/meter/{name}/rate")
	public Collection getMeterRate(@PathVariable String name) {
		Measurement[] measurements = meterTracker.getStore().get(name).toArray(new Measurement[]{});
		return IntStream.range(0, measurements.length - 1)
				.mapToObj(i -> Pair.of(measurements[i], measurements[i + 1]))
				.map(it -> new Measurement(it.getRight().x, (it.getRight().y - it.getLeft().y)
						/ Duration.between(it.getLeft().x, it.getRight().x).toSeconds()))
				.collect(Collectors.toList());
	}
}
