package com.taypo.microplot;

import io.micrometer.core.instrument.*;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MeterTracker {
	private static final Logger log = LoggerFactory.getLogger(MeterTracker.class);
	private final Map<String, Map<String, Collection<Measurement>>> store = new HashMap<>();
	private final Map<String, Meter.Type> typeMap = new HashMap<>();

	private final MeterRegistry meterRegistry;
	private final MPConfig config;

	public MeterTracker(MeterRegistry meterRegistry, MPConfig config) {
		this.meterRegistry = meterRegistry;
		this.config = config;
	}

	@Scheduled(fixedRateString = "${microplot.period:5000}")
	public void tick() {
		for (var meterName : config.getIncludeMetrics()) {
			var meters = meterRegistry.find(meterName).meters();
			log.debug("Found " + meters.size() + " meters for metric " + meterName);

			var now = LocalDateTime.now();

			for (var meter : meters) {
				var measurements = getMeasurements(meter);
				var v = readMeter(meter);
				measurements.add(new Measurement(now, v));
			}
		}
	}

	private String getTagsAsString(Meter meter) {
		var tags = meter.getId().getConventionTags(meterRegistry.config().namingConvention());
		var sb = new StringBuilder();
		sb.append("{");
		tags.stream().forEach(t -> sb.append(t.getKey() + "=\"" + t.getValue() + "\","));
		sb.append("}");
		return sb.toString();
	}

	private Collection<Measurement> getMeasurements(Meter meter) {
		var name = meter.getId().getName();
		var conventionName = meter.getId().getConventionName(meterRegistry.config().namingConvention());
		var tags = getTagsAsString(meter);
		var fullName = conventionName + tags;

		if (store.containsKey(name) == false) {
			store.put(name, new HashMap<>());
			typeMap.put(name, meter.getId().getType());
		}

		var mm = store.get(name);
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

	public Map<String, Meter.Type> getTypeMap() {
		return typeMap;
	}
}
