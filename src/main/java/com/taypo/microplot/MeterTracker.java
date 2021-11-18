package com.taypo.microplot;

import io.micrometer.core.instrument.*;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeterTracker {
	private static final Logger log = LoggerFactory.getLogger(MeterTracker.class);
	private final Map<String, Map<String, Collection<Measurement>>> store = new HashMap<>();

	private final MeterRegistry meterRegistry;
	private final MPConfig config;

	public MeterTracker(MeterRegistry meterRegistry, MPConfig config) {
		this.meterRegistry = meterRegistry;
		this.config = config;
	}

	@Scheduled(fixedRateString = "${micrometer.period:5000}")
	public void tick() {
		//meterRegistry.getMeters().stream().forEach(m -> System.out.println(m.getId().getName() + " : " + m.getClass().getName()));

		for (var meterName : config.getIncludeMetrics()) {
			var meters = meterRegistry.find(meterName).meters();
			log.debug("Found " + meters.size() + " meters for metric " + meterName);

			for (var meter : meters) {
				var name = meter.getId().getConventionName(meterRegistry.config().namingConvention());
				var tags = getTagsAsString(meter);
				var measurements = getMeasurements(meterName, name + tags);
				var v = readMeter(meter);
				measurements.add(new Measurement(LocalDateTime.now(), v));
			}
		}
	}

	private String getTagsAsString(Meter meter) {
		List<Tag> tags = meter.getId().getConventionTags(meterRegistry.config().namingConvention());
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		tags.stream().forEach(t -> sb.append(t.getKey() + "=\"" + t.getValue() + "\","));
		sb.append("}");
		return sb.toString();
	}

	private Collection<Measurement> getMeasurements(String meterName, String fullName) {
		if (store.containsKey(meterName) == false) {
			store.put(meterName, new HashMap<>());
		}
		var mm = store.get(meterName);
		if (mm.containsKey(fullName) == false) {
			mm.put(fullName, new CircularFifoQueue<>(config.getKeepRecordsMax()));
		}
		return mm.get(fullName);
	}

	private double readMeter(Meter meter) {
		if (meter instanceof Counter) {
			var counter = (Counter) meter;
			return counter.count();
		}

		if (meter instanceof Gauge) {
			var gauge = (Gauge) meter;
			return gauge.value();
		}

		return 0;
	}

	public Map<String, Map<String, Collection<Measurement>>> getStore() {
		return store;
	}

}
