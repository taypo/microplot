package com.taypo.microplot;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class MeterResource {

	private final MeterTracker meterTracker;

	public MeterResource(MeterTracker meterTracker) {
		this.meterTracker = meterTracker;
	}

	// TODO make configurable base path
	@GetMapping("/api/meter/{name}")
	public Collection getMeterData(@PathVariable String name) {
		return meterTracker.getStore().get(name);
	}

	@GetMapping("/api/meter/{name}/rate")
	public Collection getMeterRate(@PathVariable String name) {
		Measurement[] measurements = meterTracker.getStore().get(name).toArray(new Measurement[]{});
		return IntStream.range(0, measurements.length - 1)
				.mapToObj(i -> Pair.of(measurements[i], measurements[i + 1]))
				.map(it -> new Measurement(it.getRight().x, (it.getRight().y - it.getLeft().y)
						/ Duration.between(it.getLeft().x, it.getRight().x).toSeconds()))
				.collect(Collectors.toList());
	}
}
